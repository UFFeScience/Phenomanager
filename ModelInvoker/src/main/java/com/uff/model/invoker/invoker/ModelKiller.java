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
import com.uff.model.invoker.domain.ModelExecutor;
import com.uff.model.invoker.domain.ModelResultMetadata;
import com.uff.model.invoker.domain.api.google.DriveFile;
import com.uff.model.invoker.exception.GoogleErrorApiException;
import com.uff.model.invoker.exception.ModelExecutionException;

import ch.ethz.ssh2.Connection;

@Service
public abstract class ModelKiller extends ModelExtractor {
	
	private static final Logger log = LoggerFactory.getLogger(ModelKiller.class);
	
	public void stopModelExecutor(ModelExecutor modelExecutor, ExecutionEnvironment executionEnvironment)
			throws RuntimeException, Exception {
		
		if (modelExecutor == null) {
			log.warn("ModelExecutor not found");
			return;
		}
		
		if (executionEnvironment == null) {
			log.warn("ExecutionEnvironment of not found");
			return;
		}
		
		Process vpnProcess = vpnProviderService.setupVpnConfigConection(executionEnvironment.getVpnType(), 
				modelExecutor.getComputationalModel().getId(), executionEnvironment.getVpnConfiguration());
		
		if (EnvironmentType.SSH.equals(executionEnvironment.getype())) {
			handleSshEnvironmentStopExecution(executionEnvironment, modelExecutor);
			modelExecutor.setExecutionStatus(ExecutionStatus.ABORTED);
		
		} else if (EnvironmentType.CLOUD.equals(executionEnvironment.getype())) {
			handleCloudEnvironmentStopExecution(executionEnvironment, modelExecutor);
			modelExecutor.setExecutionStatus(ExecutionStatus.ABORTED);
		
		} else if (EnvironmentType.CLUSTER.equals(executionEnvironment.getype())) {
			handleClusterEnvironmentStopExecution(executionEnvironment, modelExecutor);
			modelExecutor.setExecutionStatus(ExecutionStatus.ABORTED);
		} 
		
		vpnProviderService.closeVpnConnection(vpnProcess);
	}
	
	private void handleSshEnvironmentStopExecution(ExecutionEnvironment executionEnvironment,
			ModelExecutor modelExecutor) throws Exception {

		Connection connection = null;
		ModelResultMetadata modelResultMetadata = modelResultMetadataService
				.findByModelExecutorAndExecutorExecutionStatus(modelExecutor, ExecutionStatus.RUNNING);
		if (modelResultMetadata == null || ExecutionStatus.ABORTED.equals(modelResultMetadata.getExecutionStatus()) ||
				ExecutionStatus.ABORTED.equals(modelResultMetadata.getExecutorExecutionStatus())) {
			log.warn("No modelResultMetadata found for modelExecutor of slug [{}] and executorExecutionStatus [RUNNING] or modelResultMetadata already aborted",
					modelExecutor.getSlug());
			return;
		}
		
		try {
			modelResultMetadata.appendSystemLog(String.format("Starting abort of execution of modelExecutor [%s]...", 
					modelExecutor.getTag()));
			modelResultMetadata.appendSystemLog(String.format("Starting ssh connection with environment [%s]...", 
					executionEnvironment.getTag()));
			modelResultMetadata = modelResultMetadataService.update(modelResultMetadata);
			
			connection = sshProviderService.openEnvironmentConnection(executionEnvironment.getHostAddress(),
					executionEnvironment.getUsername(), executionEnvironment.getPassword());
			
			modelResultMetadata = modelResultMetadataService
					.updateExecutionOutput(modelResultMetadata, String.format("Finished setting up ssh connection with environment [%s]", 
					executionEnvironment.getTag()));
			modelResultMetadata = runStop(modelExecutor, modelResultMetadata, connection);
			modelResultMetadata.setExecutionStatus(ExecutionStatus.ABORTED);
			
		} catch (Exception e) {
			modelResultMetadata = modelResultMetadataService
					.updateExecutionOutput(modelResultMetadata, String.format("Error while killing execution of modelExecutor [%s]", 
					modelExecutor.getTag()));
			
			throw new ModelExecutionException("Error while invoking STOP command in ssh environment");
			
		} finally {
			if (connection != null) {
				connection.close();
			}
			modelResultMetadata.setExecutionFinishDate(Calendar.getInstance());
			modelResultMetadataService.update(modelResultMetadata);
		}
	}
	
	private void handleCloudEnvironmentStopExecution(ExecutionEnvironment executionEnvironment,
			ModelExecutor modelExecutor) throws ModelExecutionException {
		
		Connection connection = null;
		ModelResultMetadata modelResultMetadata = modelResultMetadataService
				.findByModelExecutorAndExecutorExecutionStatus(modelExecutor, ExecutionStatus.RUNNING);
		if (modelResultMetadata == null || ExecutionStatus.ABORTED.equals(modelResultMetadata.getExecutionStatus()) ||
				ExecutionStatus.ABORTED.equals(modelResultMetadata.getExecutorExecutionStatus())) {
			log.warn("No modelResultMetadata found for modelExecutor of slug [{}] and executorExecutionStatus [RUNNING] or modelResultMetadata already aborted",
					modelExecutor.getSlug());
			return;
		}

		if (executionEnvironment.getVirtualMachines() == null || 
				executionEnvironment.getVirtualMachines().isEmpty()) {
			throw new ModelExecutionException("No Virtual Machine configurations available");
		}
			
		AmazonEC2Client amazonClient = cloudProviderService.authenticateProvider(executionEnvironment);
		
		try {
			modelResultMetadata.appendSystemLog(String.format("Starting abort of execution of modelExecutor [%s]  in Amazon environment...", 
					modelExecutor.getTag()));
			modelResultMetadata.appendSystemLog(String.format("Starting ssh connection with Amazon environment [%s]...", 
					executionEnvironment.getTag()));
			modelResultMetadata = modelResultMetadataService.update(modelResultMetadata);
			
			AmazonMachine amazonMachineInstance = cloudProviderService.getControlInstancesFromCluster(
					amazonClient, executionEnvironment.getClusterName());

			if (amazonMachineInstance == null) {
				modelResultMetadata = modelResultMetadataService
						.updateExecutionOutput(modelResultMetadata, "Control instance was not found");
				throw new ModelExecutionException("Control instance was not found");
			}
			try {
				modelResultMetadata = modelResultMetadataService
						.updateExecutionOutput(modelResultMetadata, String.format("Starting ssh connection with environment [%s] in Amazon control node [%s]...", 
						executionEnvironment.getTag(), amazonMachineInstance.getPublicDNS()));
				
				log.info("Executing command STOP in control node {}", amazonMachineInstance.getPublicDNS());

                connection = sshProviderService.openEnvironmentConnection(
                		amazonMachineInstance.getPublicDNS(), 
                		executionEnvironment.getUsername(), 
                		executionEnvironment.getPassword());
             
                modelResultMetadata = modelResultMetadataService
                		.updateExecutionOutput(modelResultMetadata, String.format("Finished starting ssh connection with environment [%s] in Amazon control node [%s]", 
						executionEnvironment.getTag(), amazonMachineInstance.getPublicDNS()));
                modelResultMetadata = runStop(modelExecutor, modelResultMetadata, connection);
                modelResultMetadata.setExecutionStatus(ExecutionStatus.ABORTED);
                
			} catch (Exception e) {
				modelResultMetadata = modelResultMetadataService
						.updateExecutionOutput(modelResultMetadata, String.format("Error while killing execution of modelExecutor [%s]", 
						modelExecutor.getTag()));
				
				throw new ModelExecutionException("Error while invoking STOP command in cloud environment");
				
			} finally {
				if (connection != null) {
					connection.close();
				}
				modelResultMetadata.setExecutionFinishDate(Calendar.getInstance());
				modelResultMetadataService.update(modelResultMetadata);
			}
		
		} catch (Exception e) {
			log.error("Error while creating cluster cloud from configuration", e);
		}
	}
	
	private void handleClusterEnvironmentStopExecution(ExecutionEnvironment executionEnvironment,
			ModelExecutor modelExecutor) throws Exception {
		
		Connection connection = null;
		ModelResultMetadata modelResultMetadata = modelResultMetadataService
				.findByModelExecutorAndExecutorExecutionStatus(modelExecutor, ExecutionStatus.RUNNING);
		if (modelResultMetadata == null || ExecutionStatus.ABORTED.equals(modelResultMetadata.getExecutionStatus()) ||
				ExecutionStatus.ABORTED.equals(modelResultMetadata.getExecutorExecutionStatus())) {
			log.warn("No modelResultMetadata found for modelExecutor of slug [{}] and executorExecutionStatus [RUNNING] or modelResultMetadata already aborted",
					modelExecutor.getSlug());
			return;
		}
		
		try {
			modelResultMetadata.appendSystemLog(String.format("Starting abort of execution of modelExecutor [%s]  in Cluster environment...", 
					modelExecutor.getTag()));
			modelResultMetadata.appendSystemLog(String.format("Starting ssh connection with Cluster environment [%s]...", 
					executionEnvironment.getTag()));
			modelResultMetadata = modelResultMetadataService.save(modelResultMetadata);
			
			connection = sshProviderService.openEnvironmentConnection(executionEnvironment.getHostAddress(),
					executionEnvironment.getUsername(), executionEnvironment.getPassword());
			
			modelResultMetadata = modelResultMetadataService
					.updateExecutionOutput(modelResultMetadata, String.format("Finished starting ssh connection with Cluster environment [%s]", 
					executionEnvironment.getTag()));
			modelResultMetadata = runClusterStop(modelExecutor, modelResultMetadata, connection);
			
		} catch (Exception e) {
			modelResultMetadata = modelResultMetadataService
					.updateExecutionOutput(modelResultMetadata, String.format("Error while killing execution of modelExecutor [%s]", 
					modelExecutor.getTag()));
			
			throw new ModelExecutionException("Error while invoking STOP command in cluster environment");
			
		} finally {
			if (connection != null) {
				connection.close();
			}
			modelResultMetadata.setExecutionFinishDate(Calendar.getInstance());
			modelResultMetadataService.update(modelResultMetadata);
		}
	}
	
	private ModelResultMetadata runStop(ModelExecutor modelExecutor, ModelResultMetadata modelResultMetadata, Connection connection) 
			throws IOException, InterruptedException, GoogleErrorApiException {
		modelResultMetadata = modelResultMetadataService
				.updateExecutionOutput(modelResultMetadata, String.format("Killing execution with command [%s]...", 
				modelExecutor.getTag()));
		
		byte[] abortMetadata = sshProviderService.executeCommand(connection, modelExecutor.getAbortCommand());
		
		if (abortMetadata != null && modelResultMetadata.getUploadMetadata() != null && modelResultMetadata.getUploadMetadata()) {
			DriveFile driveFile = uploadMetadata(modelResultMetadata.getSlug(), modelExecutor.getTag(), abortMetadata);
			modelResultMetadata.setAbortMetadataFileId(driveFile.getFileId());
		}
		
		modelResultMetadata = modelResultMetadataService
				.updateExecutionOutput(modelResultMetadata, String.format("Finished killing execution of modelExecutor [%s]", 
				modelExecutor.getTag()));
		
		modelResultMetadata.setExecutorExecutionStatus(ExecutionStatus.ABORTED);
		modelExecutor.setExecutionStatus(ExecutionStatus.IDLE);
		modelResultMetadata.setModelExecutor(modelExecutorService.update(modelExecutor));
		
		return modelResultMetadataService.update(modelResultMetadata);
	}
	
	private ModelResultMetadata runClusterStop(ModelExecutor modelExecutor, ModelResultMetadata modelResultMetadata, 
			Connection connection) throws IOException, InterruptedException, GoogleErrorApiException {
		modelResultMetadata = modelResultMetadataService
				.updateExecutionOutput(modelResultMetadata, String.format("Killing execution job [%s]...", 
				modelExecutor.getJobName()));
		
		byte[] abortMetadata = clusterProviderService.stopJob(connection, modelExecutor.getJobName());
		
		if (abortMetadata != null && modelResultMetadata.getUploadMetadata() != null && modelResultMetadata.getUploadMetadata()) {
			DriveFile driveFile = uploadMetadata(modelResultMetadata.getSlug(), modelExecutor.getTag(), abortMetadata);
			modelResultMetadata.setAbortMetadataFileId(driveFile.getFileId());
		}
		
		return modelResultMetadataService
				.updateExecutionOutput(modelResultMetadata, String.format("Finished sending kill command for execution job [%s]", 
				modelExecutor.getJobName()));
	}
	
}	