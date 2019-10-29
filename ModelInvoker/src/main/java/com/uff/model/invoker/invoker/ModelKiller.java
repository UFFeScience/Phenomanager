package com.uff.model.invoker.invoker;

import java.io.IOException;
import java.util.Calendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.amazonaws.services.ec2.AmazonEC2Client;
import com.uff.model.invoker.domain.AmazonMachine;
import com.uff.model.invoker.domain.EnvironmentType;
import com.uff.model.invoker.domain.ExecutionEnvironment;
import com.uff.model.invoker.domain.ExecutionStatus;
import com.uff.model.invoker.domain.ExtractorMetadata;
import com.uff.model.invoker.domain.ModelResultMetadata;
import com.uff.model.invoker.domain.api.google.DriveFile;
import com.uff.model.invoker.exception.GoogleErrorApiException;
import com.uff.model.invoker.exception.ModelExecutionException;

import ch.ethz.ssh2.Connection;

@Service
public abstract class ModelKiller extends ModelExtractor {
	
	private static final Logger log = LoggerFactory.getLogger(ModelKiller.class);
	
	public void stopModelExecutor(ModelResultMetadata modelResultMetadata, ExecutionEnvironment executionEnvironment)
			throws RuntimeException, Exception {
		
		if (modelResultMetadata == null) {
			log.warn("ModelResultMetadata not found");
			return;
		}
		
		if (executionEnvironment == null) {
			log.warn("ExecutionEnvironment of not found");
			return;
		}
		
		if (ExecutionStatus.ABORTED.equals(modelResultMetadata.getExecutionStatus()) ||
				ExecutionStatus.ABORTED.equals(modelResultMetadata.getExecutorExecutionStatus())) {
			log.warn("ModelResultMetadata of slug [{}] already aborted", modelResultMetadata.getSlug());
			return;
		}
		
		Process vpnProcess = vpnProviderService.setupVpnConfigConection(executionEnvironment.getVpnType(), 
				modelResultMetadata.getComputationalModel().getId(), executionEnvironment.getVpnConfiguration());
		
		if (EnvironmentType.SSH.equals(executionEnvironment.getype())) {
			handleSshEnvironmentStopExecution(executionEnvironment, modelResultMetadata);
		
		} else if (EnvironmentType.CLOUD.equals(executionEnvironment.getype())) {
			handleCloudEnvironmentStopExecution(executionEnvironment, modelResultMetadata);
		
		} else if (EnvironmentType.CLUSTER.equals(executionEnvironment.getype())) {
			handleClusterEnvironmentStopExecution(executionEnvironment, modelResultMetadata);
		} 
		
		vpnProviderService.closeVpnConnection(vpnProcess);
	}
	
	private void handleSshEnvironmentStopExecution(ExecutionEnvironment executionEnvironment,
			ModelResultMetadata modelResultMetadata) throws Exception {

		Connection connection = null;
		try {
			modelResultMetadata.setHasAbortRequested(Boolean.TRUE);
			modelResultMetadata = modelResultMetadataService.updateSystemLog(modelResultMetadata,
					new String[] { "Starting abort of execution of modelExecutor [%s]...", 
					String.format("Starting ssh connection with environment [%s]...", 
							executionEnvironment.getTag())}, Boolean.TRUE);
			
			connection = sshProviderService.openEnvironmentConnection(executionEnvironment.getHostAddress(),
					executionEnvironment.getUsername(), executionEnvironment.getPassword());
			
			modelResultMetadata = modelResultMetadataService.updateSystemLog(modelResultMetadata, 
					String.format("Finished setting up ssh connection with environment [%s]", 
					executionEnvironment.getTag()), Boolean.TRUE);
			
			modelResultMetadata = runStop(modelResultMetadata, connection);
			modelResultMetadata.setExecutorExecutionStatus(ExecutionStatus.ABORTED);
			
			modelResultMetadata = killExtraction(modelResultMetadata, connection);
            modelResultMetadata.setExecutionStatus(ExecutionStatus.ABORTED);
			
		} catch (Exception e) {
			modelResultMetadata = handleKillingFailure(modelResultMetadata);
			throw new ModelExecutionException("Error while invoking STOP command in ssh environment", e);
			
		} finally {
			if (connection != null) {
				connection.close();
			}
			modelResultMetadata.setExecutionFinishDate(Calendar.getInstance());
			modelResultMetadataService.update(modelResultMetadata, Boolean.TRUE);
		}
	}
	
	private void handleCloudEnvironmentStopExecution(ExecutionEnvironment executionEnvironment, ModelResultMetadata modelResultMetadata) 
			throws ModelExecutionException {
		
		Connection connection = null;
		
		if (executionEnvironment.getVirtualMachines() == null || 
				executionEnvironment.getVirtualMachines().isEmpty()) {
			throw new ModelExecutionException("No Virtual Machine configurations available");
		}
			
		try {
			AmazonEC2Client amazonClient = cloudProviderService.authenticateProvider(executionEnvironment);
			modelResultMetadata.setHasAbortRequested(Boolean.TRUE);
			modelResultMetadata = modelResultMetadataService.updateSystemLog(modelResultMetadata,
					new String[] { "Starting abort of execution in Amazon environment...", 
					String.format("Starting ssh connection with Amazon environment [%s]...", 
							executionEnvironment.getTag())}, Boolean.TRUE);
			
			AmazonMachine amazonMachineInstance = cloudProviderService.getControlInstancesFromCluster(
					amazonClient, executionEnvironment.getClusterName());

			if (amazonMachineInstance == null) {
				modelResultMetadata = modelResultMetadataService.updateSystemLog(modelResultMetadata, 
						"Control instance was not found", Boolean.TRUE);
				throw new ModelExecutionException("Control instance was not found");
			}
			modelResultMetadata = modelResultMetadataService.updateSystemLog(modelResultMetadata, 
					String.format("Starting ssh connection with environment [%s] in Amazon control node [%s]...", 
					executionEnvironment.getTag(), amazonMachineInstance.getPublicDNS()), Boolean.TRUE);
			
			log.info("Executing command STOP in control node {}", amazonMachineInstance.getPublicDNS());

            connection = sshProviderService.openEnvironmentConnection(amazonMachineInstance.getPublicDNS(), 
            		executionEnvironment.getUsername(), executionEnvironment.getPassword());
         
            modelResultMetadata = modelResultMetadataService.updateSystemLog(modelResultMetadata, 
            		String.format("Finished starting ssh connection with environment [%s] in Amazon control node [%s]", 
					executionEnvironment.getTag(), amazonMachineInstance.getPublicDNS()), Boolean.TRUE);
           
            modelResultMetadata = runStop(modelResultMetadata, connection);
            modelResultMetadata.setExecutorExecutionStatus(ExecutionStatus.ABORTED);
            
            modelResultMetadata = killExtraction(modelResultMetadata, connection);
            modelResultMetadata.setExecutionStatus(ExecutionStatus.ABORTED);
		
		} catch (Exception e) {
			modelResultMetadata = handleKillingFailure(modelResultMetadata);
			throw new ModelExecutionException("Error while invoking STOP command in cloud environment", e);
			
		} finally {
			if (connection != null) {
				connection.close();
			}
			modelResultMetadata.setExecutionFinishDate(Calendar.getInstance());
			modelResultMetadataService.update(modelResultMetadata, Boolean.TRUE);
		}
	}
	
	private void handleClusterEnvironmentStopExecution(ExecutionEnvironment executionEnvironment,
			ModelResultMetadata modelResultMetadata) throws Exception {
		
		Connection connection = null;
		try {
			modelResultMetadata.setHasAbortRequested(Boolean.TRUE);
			modelResultMetadata = modelResultMetadataService.updateSystemLog(modelResultMetadata,
					new String[] { "Starting abort of execution in Cluster environment...",
							String.format("Starting ssh connection with Cluster environment [%s]...", executionEnvironment.getTag())
					}, Boolean.TRUE);
			
			connection = sshProviderService.openEnvironmentConnection(executionEnvironment.getHostAddress(),
					executionEnvironment.getUsername(), executionEnvironment.getPassword());
			
			modelResultMetadata = modelResultMetadataService.updateSystemLog(modelResultMetadata, 
					String.format("Finished starting ssh connection with Cluster environment [%s]", executionEnvironment.getTag()), Boolean.TRUE);
			
			modelResultMetadata = runClusterStop(modelResultMetadata, connection);
			modelResultMetadata = killExtraction(modelResultMetadata, connection);
			
		} catch (Exception e) {
			modelResultMetadata = handleKillingFailure(modelResultMetadata);
			throw new ModelExecutionException("Error while invoking STOP command in cluster environment", e);
			
		} finally {
			if (connection != null) {
				connection.close();
			}
			modelResultMetadata.setExecutionFinishDate(Calendar.getInstance());
			modelResultMetadataService.update(modelResultMetadata, Boolean.TRUE);
		}
	}

	private ModelResultMetadata handleKillingFailure(ModelResultMetadata modelResultMetadata) {
		modelResultMetadata.setHasAbortRequested(Boolean.FALSE);
		modelResultMetadata = modelResultMetadataService.updateSystemLog(modelResultMetadata, "Error while killing execution", Boolean.TRUE);
		return modelResultMetadata;
	}
	
	private ModelResultMetadata runStop(ModelResultMetadata modelResultMetadata, Connection connection) 
			throws IOException, InterruptedException, GoogleErrorApiException {
		
		if (modelResultMetadata.getExecutorExecutionStatus().equals(ExecutionStatus.RUNNING) || 
				modelResultMetadata.getExecutorExecutionStatus().equals(ExecutionStatus.SCHEDULED)) {
			
			modelResultMetadata = modelResultMetadataService.updateSystemLog(modelResultMetadata, 
					String.format("Killing execution of executor with command [%s]...", modelResultMetadata.getModelExecutor().getAbortCommand()), Boolean.TRUE);
			
			byte[] abortMetadata = sshProviderService.executeCommand(connection, modelResultMetadata.getModelExecutor().getAbortCommand());
			
			if (abortMetadata != null && abortMetadata.length > 0 && modelResultMetadata.getUploadMetadata() != null && modelResultMetadata.getUploadMetadata()) {
				DriveFile driveFile = uploadMetadata(modelResultMetadata.getSlug(), 
						modelResultMetadata.getModelExecutor().getTag(), abortMetadata);
				modelResultMetadata.setAbortMetadataFileId(driveFile.getFileId());
			}
			
			modelResultMetadata = modelResultMetadataService.updateSystemLog(modelResultMetadata, 
					String.format("Finished killing execution of modelExecutor [%s]", 
							modelResultMetadata.getModelExecutor().getTag()), Boolean.TRUE);
			
			modelResultMetadata.setExecutorExecutionStatus(ExecutionStatus.ABORTED);
			modelResultMetadata = modelResultMetadataService.update(modelResultMetadata, Boolean.TRUE);
		}
		
		return modelResultMetadata;
	}
	
	private ModelResultMetadata runClusterStop(ModelResultMetadata modelResultMetadata, Connection connection) 
			throws IOException, InterruptedException, GoogleErrorApiException {
		
		if (modelResultMetadata.getExecutorExecutionStatus().equals(ExecutionStatus.RUNNING) || 
				modelResultMetadata.getExecutorExecutionStatus().equals(ExecutionStatus.SCHEDULED)) {
			
			modelResultMetadata = modelResultMetadataService.updateSystemLog(modelResultMetadata, 
					String.format("Killing execution job [%s] with command [%s]...", 
							modelResultMetadata.getModelExecutor().getJobName(), modelResultMetadata.getModelExecutor().getAbortCommand()), Boolean.TRUE);
			
			byte[] abortMetadata = clusterProviderService.stopJob(connection, modelResultMetadata.getModelExecutor().getJobName());
			
			if (abortMetadata != null && abortMetadata.length > 0 && modelResultMetadata.getUploadMetadata() != null && modelResultMetadata.getUploadMetadata()) {
				DriveFile driveFile = uploadMetadata(modelResultMetadata.getSlug(), modelResultMetadata.getModelExecutor().getTag(), abortMetadata);
				modelResultMetadata.setAbortMetadataFileId(driveFile.getFileId());
			}
			
			modelResultMetadata = modelResultMetadataService.updateSystemLog(modelResultMetadata, 
					String.format("Finished sending kill command for execution job [%s]", 
							modelResultMetadata.getModelExecutor().getJobName()), Boolean.TRUE);
		}
		
		return modelResultMetadata;
	}

	private ModelResultMetadata killExtraction(ModelResultMetadata modelResultMetadata, Connection connection)
			throws IOException, InterruptedException {
	
		if (modelResultMetadata.getExtractorMetadatas() != null && !modelResultMetadata.getExtractorMetadatas().isEmpty()) {
			for (ExtractorMetadata extractorMetadata : modelResultMetadata.getExtractorMetadatas()) {
				if (extractorMetadata.getExecutionStatus().equals(ExecutionStatus.RUNNING) || 
						extractorMetadata.getExecutionStatus().equals(ExecutionStatus.SCHEDULED)) {
					
					modelResultMetadata = modelResultMetadataService
							.updateSystemLog(modelResultMetadata, String.format("Killing extraction of extractor [%s] with command [%s]...", 
									extractorMetadata.getModelMetadataExtractor().getTag(), 
									extractorMetadata.getModelMetadataExtractor().getAbortCommand()), Boolean.TRUE);
					
					sshProviderService.executeCommand(connection, modelResultMetadata.getModelExecutor().getAbortCommand());

					modelResultMetadata = modelResultMetadataService
							.updateSystemLog(modelResultMetadata, String.format("Finished killing extraction of extractor [%s]", 
									extractorMetadata.getModelMetadataExtractor().getTag()), Boolean.TRUE);
				}
				extractorMetadata = extractorMetadataService.update(extractorMetadata);
			}
			modelResultMetadata = modelResultMetadataService.update(modelResultMetadata, Boolean.TRUE);
		}
		
		return modelResultMetadata;
	}
	
}	