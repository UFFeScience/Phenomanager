package com.uff.model.invoker.invoker;

import java.util.Calendar;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.amazonaws.services.ec2.AmazonEC2Client;
import com.uff.model.invoker.Constants;
import com.uff.model.invoker.domain.AmazonMachine;
import com.uff.model.invoker.domain.EnvironmentType;
import com.uff.model.invoker.domain.ExecutionEnvironment;
import com.uff.model.invoker.domain.ExecutionStatus;
import com.uff.model.invoker.domain.ModelExecutor;
import com.uff.model.invoker.domain.ModelResultMetadata;
import com.uff.model.invoker.domain.User;
import com.uff.model.invoker.exception.AbortedExecutionException;
import com.uff.model.invoker.exception.ModelExecutionException;

import ch.ethz.ssh2.Connection;

@Service
public abstract class ModelInvoker extends ModelKiller {
	
	private static final Logger log = LoggerFactory.getLogger(ModelInvoker.class);
	
	public void startModelExecutor(ModelExecutor modelExecutor, ExecutionEnvironment executionEnvironment, User userAgent,
			List<String> executionExtractors, Boolean uploadMetadataToDrive)
			throws RuntimeException, Exception {
		
		if (modelExecutor == null) {
			throw new ModelExecutionException("ModelExecutor not found");
		}
		
		if (executionEnvironment == null) {
			throw new ModelExecutionException("ExecutionEnvironment of not found");
		}
		
		Process vpnProcess = vpnProviderService.setupVpnConfigConection(executionEnvironment.getVpnType(), 
				modelExecutor.getComputationalModel().getId(), executionEnvironment.getVpnConfiguration());
		
		if (EnvironmentType.SSH.equals(executionEnvironment.getype())) {
			handleSshEnvironmentStartExecution(executionEnvironment, modelExecutor, userAgent, executionExtractors, uploadMetadataToDrive);
			
		} else if (EnvironmentType.CLOUD.equals(executionEnvironment.getype())) {
			handleCloudEnvironmentStartExecution(executionEnvironment, modelExecutor, userAgent, executionExtractors, uploadMetadataToDrive);
			
		} else if (EnvironmentType.CLUSTER.equals(executionEnvironment.getype())) {
			handleClusterEnvironmentStartExecution(executionEnvironment, modelExecutor, userAgent, executionExtractors, uploadMetadataToDrive);
		} 
		
		vpnProviderService.closeVpnConnection(vpnProcess);
	}

	private void handleSshEnvironmentStartExecution(ExecutionEnvironment executionEnvironment,
			ModelExecutor modelExecutor, User userAgent, List<String> executionExtractors, Boolean uploadMetadata) throws Exception {
		
		Connection connection = null;
		ModelResultMetadata modelResultMetadata = setupModelResultMetadata(executionEnvironment, modelExecutor, userAgent,
				executionExtractors, uploadMetadata);
		try {
			modelResultMetadata = modelResultMetadataService.updateSystemLog(modelResultMetadata,
					new String[] { String.format("Starting execution of modelExecutor [%s]...", 
							modelResultMetadata.getModelExecutor().getTag()), 
							String.format("Starting ssh connection with environment [%s]...", 
									executionEnvironment.getTag())});
			
			connection = sshProviderService.openEnvironmentConnection(executionEnvironment.getHostAddress(),
					executionEnvironment.getUsername(), executionEnvironment.getPassword());
			
			modelResultMetadata = modelResultMetadataService.updateSystemLog(modelResultMetadata, 
					String.format("Finished setting up ssh connection with environment [%s]", executionEnvironment.getTag()));
			
			if (ExecutionStatus.SCHEDULED.equals(modelResultMetadata.getExecutionStatus()) || 
					ExecutionStatus.RUNNING.equals(modelResultMetadata.getExecutionStatus())) {
				modelResultMetadata = runInSsh(modelResultMetadata.getModelExecutor(), modelResultMetadata, connection);
				modelResultMetadata.setExecutorExecutionStatus(ExecutionStatus.FINISHED);
				modelResultMetadata = modelResultMetadataService.update(modelResultMetadata);
			}

			modelResultMetadata = handleExtractorExecution(connection, executionEnvironment.getComputationalModel(), modelResultMetadata);
			modelResultMetadata = checkExecutionExtractionStatus(modelResultMetadata);
		
		} catch (AbortedExecutionException e) {
			log.warn("Task was aborted during execution", e);
			modelResultMetadata = handlePendingExtraction(modelResultMetadata);
			
		} catch (Exception e) {
			modelResultMetadata = handleInvokingFailure(modelExecutor, modelResultMetadata);
			throw new ModelExecutionException("Error while invoking START command in ssh environment", e);
			
		} finally {
			if (connection != null) {
				connection.close();
			}
			modelResultMetadata.setExecutionFinishDate(Calendar.getInstance());
			modelResultMetadataService.update(modelResultMetadata);
		}
	}

	private void handleCloudEnvironmentStartExecution(ExecutionEnvironment executionEnvironment,
			ModelExecutor modelExecutor, User userAgent, List<String> executionExtractors, Boolean uploadMetadata) throws ModelExecutionException {
		
		Connection connection = null;
		ModelResultMetadata modelResultMetadata = setupModelResultMetadata(executionEnvironment, modelExecutor, userAgent,
				executionExtractors, uploadMetadata);
		
		if (executionEnvironment.getVirtualMachines() == null || 
				executionEnvironment.getVirtualMachines().isEmpty()) {
			throw new ModelExecutionException("No Virtual Machine configurations available");
		}
		
		try {
			modelResultMetadata = modelResultMetadataService.updateSystemLog(modelResultMetadata,
					new String[] { String.format("Starting execution of modelExecutor [%s] in Amazon environment...", 
							modelResultMetadata.getModelExecutor().getTag()), 
							String.format("Setting up Amazon environment [%s]...", 
									executionEnvironment.getTag())});
			
			AmazonEC2Client amazonClient = cloudProviderService.authenticateProvider(executionEnvironment);
			cloudProviderService.createCluster(amazonClient, executionEnvironment, Constants.USER_HOME_DIR);
			AmazonMachine amazonMachineInstance = cloudProviderService.getControlInstancesFromCluster(
					amazonClient, executionEnvironment.getClusterName());

			if (amazonMachineInstance == null) {
				modelResultMetadata = modelResultMetadataService
						.updateSystemLog(modelResultMetadata, "Control instance was not found");
				throw new ModelExecutionException("Control instance was not found");
                
			} else {
				modelResultMetadata = modelResultMetadataService
						.updateSystemLog(modelResultMetadata, 
								String.format("Starting ssh connection with environment [%s] in Amazon control node [%s]...", 
						executionEnvironment.getTag(), amazonMachineInstance.getPublicDNS()));
				
				log.info("Executing command START in control node [{}]", amazonMachineInstance.getPublicDNS());

                connection = sshProviderService.openEnvironmentConnection(amazonMachineInstance.getPublicDNS(), 
                		executionEnvironment.getUsername(), executionEnvironment.getPassword());
                
                modelResultMetadata = modelResultMetadataService.updateSystemLog(modelResultMetadata, 
                				String.format("Finished starting ssh connection with environment [%s] in Amazon control node [%s]", 
						executionEnvironment.getTag(), amazonMachineInstance.getPublicDNS()));
                
                if (ExecutionStatus.SCHEDULED.equals(modelResultMetadata.getExecutionStatus()) || 
    					ExecutionStatus.RUNNING.equals(modelResultMetadata.getExecutionStatus())) {
                	modelResultMetadata = runInCloud(modelResultMetadata.getModelExecutor(), modelResultMetadata, connection);
                	modelResultMetadata.setExecutorExecutionStatus(ExecutionStatus.FINISHED);
                	modelResultMetadata = modelResultMetadataService.update(modelResultMetadata);
                }
            
                modelResultMetadata = handleExtractorExecution(connection, executionEnvironment.getComputationalModel(), modelResultMetadata);
            	modelResultMetadata = checkExecutionExtractionStatus(modelResultMetadata);
			}
		
		} catch (AbortedExecutionException e) {
			log.warn("Task was aborted during execution", e);
			modelResultMetadata = handlePendingExtraction(modelResultMetadata);
			
		} catch (Exception e) {
			modelResultMetadata = handleInvokingFailure(modelExecutor, modelResultMetadata);
			throw new ModelExecutionException("Error while invoking START command in cloud environment", e);
			
		} finally {
			if (connection != null) {
				connection.close();
			}
			modelResultMetadata.setExecutionFinishDate(Calendar.getInstance());
			modelResultMetadataService.update(modelResultMetadata);
		}
	}
	
	private void handleClusterEnvironmentStartExecution(ExecutionEnvironment executionEnvironment,
			ModelExecutor modelExecutor, User userAgent, List<String> executionExtractors, Boolean uploadMetadata) throws Exception {

		Connection connection = null;
		ModelResultMetadata modelResultMetadata = setupModelResultMetadata(executionEnvironment, modelExecutor, userAgent,
				executionExtractors, uploadMetadata);
		try {
			modelResultMetadata = modelResultMetadataService.updateSystemLog(modelResultMetadata,
					new String[] { String.format("Starting execution of modelExecutor [%s] in Cluster environment...", 
							modelResultMetadata.getModelExecutor().getTag()), 
							String.format("Starting ssh connection with Cluster environment [%s]...", 
									executionEnvironment.getTag())});
			
			connection = sshProviderService.openEnvironmentConnection(executionEnvironment.getHostAddress(),
					executionEnvironment.getUsername(), executionEnvironment.getPassword());
			
			modelResultMetadata = modelResultMetadataService.updateSystemLog(modelResultMetadata, 
					String.format("Finished starting ssh connection with Cluster environment [%s]", executionEnvironment.getTag()));
			
			if (ExecutionStatus.SCHEDULED.equals(modelResultMetadata.getExecutionStatus()) || 
					ExecutionStatus.RUNNING.equals(modelResultMetadata.getExecutionStatus())) {
				modelResultMetadata = runInCluster(modelResultMetadata.getModelExecutor(), modelResultMetadata, connection);
			}
			
		} catch (AbortedExecutionException e) {
			log.warn("Task was aborted during execution", e);
			modelResultMetadata = handlePendingExtraction(modelResultMetadata);
			
		} catch (Exception e) {
			modelResultMetadata = handleInvokingFailure(modelExecutor, modelResultMetadata);
			throw new ModelExecutionException("Error while invoking START command in cluster environment", e);
			
		} finally {
			if (connection != null) {
				connection.close();
			}
			modelResultMetadata.setExecutionFinishDate(Calendar.getInstance());
			modelResultMetadataService.update(modelResultMetadata);
		}
	}

	private ModelResultMetadata handleInvokingFailure(ModelExecutor modelExecutor, ModelResultMetadata modelResultMetadata) {
		modelResultMetadata = modelResultMetadataService.findBySlug(modelResultMetadata.getSlug());
	
		if (!modelResultMetadata.getExecutionStatus().equals(ExecutionStatus.ABORTED) && 
				!modelResultMetadata.getExecutorExecutionStatus().equals(ExecutionStatus.ABORTED)) {
			modelResultMetadata.setExecutionStatus(ExecutionStatus.FAILURE);
			modelResultMetadata.setExecutorExecutionStatus(ExecutionStatus.FAILURE);
			modelResultMetadata = modelResultMetadataService
					.updateSystemLog(modelResultMetadata, String.format("Error while starting modelExecutor [%s]", 
							modelExecutor.getTag()));
		}
		
		return handlePendingExtraction(modelResultMetadata);
	}

	private ModelResultMetadata setupModelResultMetadata(ExecutionEnvironment executionEnvironment, ModelExecutor modelExecutor, 
			User userAgent, List<String> executionExtractors, Boolean uploadMetadata) {
		
		ModelResultMetadata modelResultMetadata = modelResultMetadataService
				.findByModelExecutorAndExecutionEnvironmentAndExecutionStatus(modelExecutor, executionEnvironment, ExecutionStatus.RUNNING);
		
		if (modelResultMetadata != null) {
			return modelResultMetadata;
		}
		
		modelResultMetadata = modelResultMetadataService.save(ModelResultMetadata.builder()
			.computationalModel(executionEnvironment.getComputationalModel())
			.modelExecutor(modelExecutor)
			.executionEnvironment(executionEnvironment)
			.userAgent(userAgent)
			.executionStartDate(Calendar.getInstance())
			.executorExecutionStatus(ExecutionStatus.RUNNING)
			.uploadMetadata(uploadMetadata)
			.build());
		
		modelResultMetadata.setExtractorMetadatas(getExecutionExtractors(executionExtractors , modelResultMetadata));
		return modelResultMetadataService.update(modelResultMetadata);
	}
	
}	