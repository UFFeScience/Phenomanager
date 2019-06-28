package com.uff.model.invoker.invoker;

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
import com.uff.model.invoker.exception.ModelExecutionException;

import ch.ethz.ssh2.Connection;

@Service
public abstract class ModelInvoker extends ModelKiller {
	
	private static final Logger log = LoggerFactory.getLogger(ModelInvoker.class);
	
	public void startModelExecutor(ModelExecutor modelExecutor)
			throws RuntimeException, Exception {
		
		if (modelExecutor == null) {
			throw new ModelExecutionException("ModelExecutor not found");
		}
		
		ExecutionEnvironment executionEnvironment = executionEnvironmentService
				.findByComputationalModelAndActive(modelExecutor.getComputationalModel(), Boolean.TRUE);
	
		if (executionEnvironment == null) {
			throw new ModelExecutionException(String.format("ExecutionEnvironment of ComputationalModel of slug [%s], not found", 
					modelExecutor.getComputationalModel().getSlug()));
		}
		
		Process vpnProcess = vpnProvider.setupVpnConfigConection(executionEnvironment.getVpnType(), 
				modelExecutor.getComputationalModel().getId(), executionEnvironment.getVpnConfiguration());
		
		if (EnvironmentType.SSH.equals(executionEnvironment.getype())) {
			handleSshEnvironmentStartExecution(executionEnvironment, modelExecutor);
			modelExecutor.setExecutionStatus(ExecutionStatus.FINISHED);
			
		} else if (EnvironmentType.CLOUD.equals(executionEnvironment.getype())) {
			handleCloudEnvironmentStartExecution(executionEnvironment, modelExecutor);
			modelExecutor.setExecutionStatus(ExecutionStatus.FINISHED);
			
		} else if (EnvironmentType.CLUSTER.equals(executionEnvironment.getype())) {
			handleClusterEnvironmentStartExecution(executionEnvironment, modelExecutor);
		} 
		
		vpnProvider.closeVpnConnection(vpnProcess);
	}

	private void handleSshEnvironmentStartExecution(ExecutionEnvironment executionEnvironment,
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
			modelResultMetadata.appendExecutionLog(String.format("Starting execution of modelExecutor [%s]...", 
					modelExecutor.getTag()));
			modelResultMetadata.appendExecutionLog(String.format("Starting ssh connection with environment [%s]...", 
					executionEnvironment.getTag()));
			
			modelResultMetadata = modelResultMetadataService.save(modelResultMetadata);
			
			connection = sshProvider.openEnvironmentConnection(executionEnvironment.getHostAddress(),
					executionEnvironment.getUsername(), executionEnvironment.getPassword());
			
			modelResultMetadata = updateExecutionOutput(modelResultMetadata, String.format("Finished setting up ssh connection with environment [%s]", 
					executionEnvironment.getTag()));
			
			runInSsh(modelExecutor, modelResultMetadata, connection);
			modelResultMetadata = handleExtractorExecution(connection, executionEnvironment.getComputationalModel(), modelResultMetadata);
			
		} catch (Exception e) {
			modelResultMetadata.setExecutionStatus(ExecutionStatus.FAILURE);
			
			modelResultMetadata = updateExecutionOutput(modelResultMetadata, String.format("Error while starting modelExecutor [%s]", 
					modelExecutor.getTag()));
			
			throw new ModelExecutionException("Error while invoking START command in ssh environment");
			
		} finally {
			if (connection != null) {
				connection.close();
			}
			
			modelResultMetadata.setExecutionStatus(ExecutionStatus.FINISHED);
			modelResultMetadata.setExecutionFinishDate(Calendar.getInstance());
			modelResultMetadataService.update(modelResultMetadata);
		}
	}
	
	private void handleCloudEnvironmentStartExecution(ExecutionEnvironment executionEnvironment,
			ModelExecutor modelExecutor) throws ModelExecutionException {
		
		Connection connection = null;
		ModelResultMetadata modelResultMetadata = modelResultMetadataService.save(ModelResultMetadata.builder()
			.computationalModel(executionEnvironment.getComputationalModel())
			.modelExecutor(modelExecutor)
			.executionEnvironment(executionEnvironment)
			.userAgent(modelExecutor.getUserAgent())
			.executionStartDate(Calendar.getInstance())
			.build());
		
		if (executionEnvironment.getVirtualMachines() == null || 
				executionEnvironment.getVirtualMachines().isEmpty()) {
			throw new ModelExecutionException("No Virtual Machine configurations available");
		}
			
		try {
			modelResultMetadata.appendExecutionLog(String.format("Starting execution of modelExecutor [%s] in Amazon environment...", 
					modelExecutor.getTag()));
			modelResultMetadata.appendExecutionLog(String.format("Setting up Amazon environment [%s]...", 
					executionEnvironment.getTag()));
			
			modelResultMetadata = modelResultMetadataService.save(modelResultMetadata);
			
			AmazonEC2Client amazonClient = cloudProvider.authenticateProvider(executionEnvironment);

			String absolutePath = System.getProperty("user.dir");
			cloudProvider.createCluster(amazonClient, executionEnvironment, absolutePath);

			AmazonMachine amazonMachineInstance = cloudProvider.getControlInstancesFromCluster(
					amazonClient, executionEnvironment.getClusterName());

			if (amazonMachineInstance == null) {
				modelResultMetadata = updateExecutionOutput(modelResultMetadata, "Control instance was not found");
				throw new ModelExecutionException("Control instance was not found");
                
			} else {
				try {
					modelResultMetadata = updateExecutionOutput(modelResultMetadata, String.format("Starting ssh connection with environment [%s] in Amazon control node [%s]...", 
							executionEnvironment.getTag(), amazonMachineInstance.getPublicDNS()));
					
					log.info("Executing command START in control node [{}]", amazonMachineInstance.getPublicDNS());

	                connection = sshProvider.openEnvironmentConnection(
	                		amazonMachineInstance.getPublicDNS(), 
	                		executionEnvironment.getUsername(), 
	                		executionEnvironment.getPassword());
	                
	                modelResultMetadata = updateExecutionOutput(modelResultMetadata, String.format("Finished starting ssh connection with environment [%s] in Amazon control node [%s]", 
							executionEnvironment.getTag(), amazonMachineInstance.getPublicDNS()));
	                
                	runInCloud(modelExecutor, modelResultMetadata, connection);
                	modelResultMetadata = handleExtractorExecution(connection, executionEnvironment.getComputationalModel(), modelResultMetadata);
				
				} catch (Exception e) {
					modelResultMetadata.setExecutionStatus(ExecutionStatus.FAILURE);
					
					modelResultMetadata = updateExecutionOutput(modelResultMetadata, String.format("Error while starting modelExecutor [%s]", 
							modelExecutor.getTag()));
					
					throw new ModelExecutionException("Error while invoking START command in cloud environment");
					
				} finally {
					if (connection != null) {
						connection.close();
					}
					
					modelResultMetadata.setExecutionStatus(ExecutionStatus.FINISHED);
					modelResultMetadata.setExecutionFinishDate(Calendar.getInstance());
					modelResultMetadataService.update(modelResultMetadata);
				}
			}
		
		} catch (Exception e) {
			log.error("Error while creating cluster cloud from configuration", e);
		}
	}
	
	private void handleClusterEnvironmentStartExecution(ExecutionEnvironment executionEnvironment,
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
			modelResultMetadata.appendExecutionLog(String.format("Starting execution of modelExecutor [%s] in Cluster environment...", 
					modelExecutor.getTag()));
			modelResultMetadata.appendExecutionLog(String.format("Starting ssh connection with Cluster environment [%s]...", 
					executionEnvironment.getTag()));
			
			modelResultMetadata = modelResultMetadataService.save(modelResultMetadata);
			
			connection = sshProvider.openEnvironmentConnection(executionEnvironment.getHostAddress(),
					executionEnvironment.getUsername(), executionEnvironment.getPassword());
			
			modelResultMetadata = updateExecutionOutput(modelResultMetadata, String.format("Finished starting ssh connection with Cluster environment [%s]", 
					executionEnvironment.getTag()));
			
			runInCluster(modelExecutor, modelResultMetadata, connection);
			modelResultMetadata = handleExtractorExecution(connection, executionEnvironment.getComputationalModel(), modelResultMetadata);

		} catch (Exception e) {
			modelResultMetadata.setExecutionStatus(ExecutionStatus.FAILURE);
			
			modelResultMetadata = updateExecutionOutput(modelResultMetadata, String.format("Error while starting modelExecutor [%s]", 
					modelExecutor.getTag()));
			
			throw new ModelExecutionException("Error while invoking START command in cluster environment");
			
		} finally {
			if (connection != null) {
				connection.close();
			}
			
			modelResultMetadata.setExecutionStatus(ExecutionStatus.FINISHED);
			modelResultMetadata.setExecutionFinishDate(Calendar.getInstance());
			modelResultMetadataService.update(modelResultMetadata);
		}
	}
	
}	