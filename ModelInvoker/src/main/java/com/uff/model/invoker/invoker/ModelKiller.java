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
	
	public void stopModelExecutor(ModelExecutor modelExecutor)
			throws RuntimeException, Exception {
		
		if (modelExecutor == null) {
			log.warn("ModelExecutor not found");
			return;
		}
		
		ExecutionEnvironment executionEnvironment = executionEnvironmentService
				.findByComputationalModelAndActive(modelExecutor.getComputationalModel(), Boolean.TRUE);
	
		if (executionEnvironment == null) {
			log.warn("ExecutionEnvironment of ComputationalModel of slug [{}], not found", modelExecutor.getComputationalModel().getSlug());
			return;
		}
		
		Process vpnProcess = vpnProvider.setupVpnConfigConection(executionEnvironment.getVpnType(), 
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
		
		vpnProvider.closeVpnConnection(vpnProcess);
	}
	
	private void handleSshEnvironmentStopExecution(ExecutionEnvironment executionEnvironment,
			ModelExecutor modelExecutor) throws Exception {

		Connection connection = null;
		ModelResultMetadata modelResultMetadata = modelResultMetadataService.save(ModelResultMetadata.builder()
				.computationalModel(executionEnvironment.getComputationalModel())
				.modelExecutor(modelExecutor)
				.executionEnvironment(executionEnvironment)
				.userAgent(modelExecutor.getUserAgent())
				.executionStartDate(Calendar.getInstance())
				.build());
		
		try {
			modelResultMetadata.appendExecutionLog(String.format("Starting abort of execution of modelExecutor [%s]...", 
					modelExecutor.getTag()));
			modelResultMetadata.appendExecutionLog(String.format("Starting ssh connection with environment [%s]...", 
					executionEnvironment.getTag()));
			
			modelResultMetadata = modelResultMetadataService.save(modelResultMetadata);
			
			connection = sshProvider.openEnvironmentConnection(executionEnvironment.getHostAddress(),
					executionEnvironment.getUsername(), executionEnvironment.getPassword());
			
			modelResultMetadata = updateExecutionOutput(modelResultMetadata, String.format("Finished setting up ssh connection with environment [%s]", 
					executionEnvironment.getTag()));
			
			runStop(modelExecutor, modelResultMetadata, connection);
	
		} catch (Exception e) {
			modelResultMetadata.setExecutionStatus(ExecutionStatus.FAILURE);
			
			modelResultMetadata = updateExecutionOutput(modelResultMetadata, String.format("Error while killing execution of modelExecutor [%s]", 
					modelExecutor.getTag()));
			
			throw new ModelExecutionException("Error while invoking STOP command in ssh environment");
			
		} finally {
			if (connection != null) {
				connection.close();
			}
			
			modelResultMetadata.setExecutionStatus(ExecutionStatus.FINISHED);
			modelResultMetadata.setExecutionFinishDate(Calendar.getInstance());
			modelResultMetadataService.update(modelResultMetadata);
		}
	}
	
	private void handleCloudEnvironmentStopExecution(ExecutionEnvironment executionEnvironment,
			ModelExecutor modelExecutor) throws ModelExecutionException {
		
		Connection connection = null;
		ModelResultMetadata modelResultMetadata = modelResultMetadataService.save(ModelResultMetadata.builder()
				.computationalModel(executionEnvironment.getComputationalModel())
				.executionEnvironment(executionEnvironment)
				.modelExecutor(modelExecutor)
				.userAgent(modelExecutor.getUserAgent())
				.executionStartDate(Calendar.getInstance())
				.build());

		if (executionEnvironment.getVirtualMachines() == null || 
				executionEnvironment.getVirtualMachines().isEmpty()) {
			throw new ModelExecutionException("No Virtual Machine configurations available");
		}
			
		AmazonEC2Client amazonClient = cloudProvider.authenticateProvider(executionEnvironment);

		try {
			modelResultMetadata.appendExecutionLog(String.format("Starting abort of execution of modelExecutor [%s]  in Amazon environment...", 
					modelExecutor.getTag()));
			modelResultMetadata.appendExecutionLog(String.format("Starting ssh connection with Amazon environment [%s]...", 
					executionEnvironment.getTag()));
			
			modelResultMetadata = modelResultMetadataService.save(modelResultMetadata);
			
			AmazonMachine amazonMachineInstance = cloudProvider.getControlInstancesFromCluster(
					amazonClient, executionEnvironment.getClusterName());

			if (amazonMachineInstance == null) {
				modelResultMetadata = updateExecutionOutput(modelResultMetadata, "Control instance was not found");
				throw new ModelExecutionException("Control instance was not found");
			}
				
			try {
				modelResultMetadata = updateExecutionOutput(modelResultMetadata, String.format("Starting ssh connection with environment [%s] in Amazon control node [%s]...", 
						executionEnvironment.getTag(), amazonMachineInstance.getPublicDNS()));
				
				log.info("Executing command STOP in control node {}", amazonMachineInstance.getPublicDNS());

                connection = sshProvider.openEnvironmentConnection(
                		amazonMachineInstance.getPublicDNS(), 
                		executionEnvironment.getUsername(), 
                		executionEnvironment.getPassword());
             
                modelResultMetadata = updateExecutionOutput(modelResultMetadata, String.format("Finished starting ssh connection with environment [%s] in Amazon control node [%s]", 
						executionEnvironment.getTag(), amazonMachineInstance.getPublicDNS()));
                
                runStop(modelExecutor, modelResultMetadata, connection);
			
			} catch (Exception e) {
				modelResultMetadata.setExecutionStatus(ExecutionStatus.FAILURE);
				
				modelResultMetadata = updateExecutionOutput(modelResultMetadata, String.format("Error while killing execution of modelExecutor [%s]", 
						modelExecutor.getTag()));
				
				throw new ModelExecutionException("Error while invoking STOP command in cloud environment");
				
			} finally {
				if (connection != null) {
					connection.close();
				}
				
				modelResultMetadata.setExecutionStatus(ExecutionStatus.FINISHED);
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
		ModelResultMetadata modelResultMetadata = modelResultMetadataService.save(ModelResultMetadata.builder()
				.computationalModel(executionEnvironment.getComputationalModel())
				.modelExecutor(modelExecutor)
				.executionEnvironment(executionEnvironment)
				.userAgent(modelExecutor.getUserAgent())
				.executionStartDate(Calendar.getInstance())
				.build());
		try {
			modelResultMetadata.appendExecutionLog(String.format("Starting abort of execution of modelExecutor [%s]  in Cluster environment...", 
					modelExecutor.getTag()));
			modelResultMetadata.appendExecutionLog(String.format("Starting ssh connection with Cluster environment [%s]...", 
					executionEnvironment.getTag()));
			
			modelResultMetadata = modelResultMetadataService.save(modelResultMetadata);
			
			connection = sshProvider.openEnvironmentConnection(executionEnvironment.getHostAddress(),
					executionEnvironment.getUsername(), executionEnvironment.getPassword());
			
			modelResultMetadata = updateExecutionOutput(modelResultMetadata, String.format("Finished starting ssh connection with Cluster environment [%s]", 
					executionEnvironment.getTag()));
			
			runClusterStop(modelExecutor, modelResultMetadata, connection);
	
		} catch (Exception e) {
			modelResultMetadata.setExecutionStatus(ExecutionStatus.FAILURE);
			
			modelResultMetadata = updateExecutionOutput(modelResultMetadata, String.format("Error while killing execution of modelExecutor [%s]", 
					modelExecutor.getTag()));
			
			throw new ModelExecutionException("Error while invoking STOP command in cluster environment");
			
		} finally {
			if (connection != null) {
				connection.close();
			}
			
			modelResultMetadata.setExecutionStatus(ExecutionStatus.FINISHED);
			modelResultMetadata.setExecutionFinishDate(Calendar.getInstance());
			modelResultMetadataService.update(modelResultMetadata);
		}
	}
	
	private void runStop(ModelExecutor modelExecutor, ModelResultMetadata modelResultMetadata, Connection connection) throws IOException, InterruptedException, GoogleErrorApiException {
		modelResultMetadata = updateExecutionOutput(modelResultMetadata, String.format("Killing execution with command [%s]...", 
				modelExecutor.getTag()));
		
		byte[] abortMetadata = sshProvider.executeCommand(connection, modelExecutor.getAbortCommand());
		
		if (abortMetadata != null) {
			DriveFile driveFile = uploadMetadata(modelResultMetadata.getSlug(), modelExecutor.getTag(), abortMetadata);
			modelResultMetadata.setAbortMetadataFileId(driveFile.getFileId());
		}
		
		modelResultMetadata = updateExecutionOutput(modelResultMetadata, String.format("Finished killing execution of modelExecutor [%s]", 
				modelExecutor.getTag()));
		
		modelExecutor.setExecutionStatus(ExecutionStatus.FINISHED);
	}
	
	private void runClusterStop(ModelExecutor modelExecutor, ModelResultMetadata modelResultMetadata, Connection connection) throws IOException, InterruptedException, GoogleErrorApiException {
		modelResultMetadata = updateExecutionOutput(modelResultMetadata, String.format("Killing execution job [%s]...", 
				modelExecutor.getJobName()));
		
		byte[] abortMetadata = clusterProvider.stopJob(connection, modelExecutor.getJobName());
		
		if (abortMetadata != null) {
			DriveFile driveFile = uploadMetadata(modelResultMetadata.getSlug(), modelExecutor.getTag(), abortMetadata);
			modelResultMetadata.setAbortMetadataFileId(driveFile.getFileId());
		}
		
		modelResultMetadata = updateExecutionOutput(modelResultMetadata, String.format("Finished killing execution jof of [%s]", 
				modelExecutor.getJobName()));
		
		modelExecutor.setExecutionStatus(ExecutionStatus.FINISHED);
	}
	
}	