package com.uff.model.invoker.invoker;

import java.util.Calendar;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.amazonaws.services.ec2.AmazonEC2Client;
import com.uff.model.invoker.domain.AmazonMachine;
import com.uff.model.invoker.domain.EnvironmentType;
import com.uff.model.invoker.domain.ExecutionEnvironment;
import com.uff.model.invoker.domain.ExecutionStatus;
import com.uff.model.invoker.domain.ExtractorMetadata;
import com.uff.model.invoker.domain.ModelExecutor;
import com.uff.model.invoker.domain.ModelResultMetadata;
import com.uff.model.invoker.exception.AbortedExecutionException;
import com.uff.model.invoker.exception.ModelExecutionException;

import ch.ethz.ssh2.Connection;

@Service
public abstract class ModelInvoker extends ModelKiller {
	
	private static final Logger log = LoggerFactory.getLogger(ModelInvoker.class);
	
	public void startModelExecutor(ModelExecutor modelExecutor, ExecutionEnvironment executionEnvironment, 
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
			handleSshEnvironmentStartExecution(executionEnvironment, modelExecutor, executionExtractors, uploadMetadataToDrive);
			
		} else if (EnvironmentType.CLOUD.equals(executionEnvironment.getype())) {
			handleCloudEnvironmentStartExecution(executionEnvironment, modelExecutor, executionExtractors, uploadMetadataToDrive);
			
		} else if (EnvironmentType.CLUSTER.equals(executionEnvironment.getype())) {
			handleClusterEnvironmentStartExecution(executionEnvironment, modelExecutor, executionExtractors, uploadMetadataToDrive);
		} 
		
		vpnProviderService.closeVpnConnection(vpnProcess);
	}

	private void handleSshEnvironmentStartExecution(ExecutionEnvironment executionEnvironment,
			ModelExecutor modelExecutor, List<String> executionExtractors, Boolean uploadMetadata) throws Exception {
		
		Connection connection = null;
		ModelResultMetadata modelResultMetadata = setupModelResultMetadata(executionEnvironment, modelExecutor,
				executionExtractors, uploadMetadata);
		try {
			modelResultMetadata.appendSystemLog(String.format("Starting execution of modelExecutor [%s]...", 
					modelResultMetadata.getModelExecutor().getTag()));
			modelResultMetadata.appendSystemLog(String.format("Starting ssh connection with environment [%s]...", 
					executionEnvironment.getTag()));
			modelResultMetadata = modelResultMetadataService.update(modelResultMetadata);
			
			connection = sshProviderService.openEnvironmentConnection(executionEnvironment.getHostAddress(),
					executionEnvironment.getUsername(), executionEnvironment.getPassword());
			
			modelResultMetadata = modelResultMetadataService
					.updateExecutionOutput(modelResultMetadata, String.format("Finished setting up ssh connection with environment [%s]", 
					executionEnvironment.getTag()));
			modelResultMetadata = runInSsh(modelResultMetadata.getModelExecutor(), modelResultMetadata, connection);
			modelResultMetadata = handleExtractorExecution(connection, executionEnvironment.getComputationalModel(), 
					modelResultMetadata);
			modelResultMetadata.setExecutionStatus(ExecutionStatus.FINISHED);
		
		} catch (AbortedExecutionException e) {
			log.warn("Task was aborted during execution", e);
			modelResultMetadata = modelResultMetadataService.findBySlug(modelResultMetadata.getSlug());
			modelResultMetadata = handlePendingExtraction(modelResultMetadata);
			
		} catch (Exception e) {
			modelResultMetadata.setExecutionStatus(ExecutionStatus.FAILURE);
			modelResultMetadata.setExecutorExecutionStatus(ExecutionStatus.FAILURE);
			modelResultMetadata = modelResultMetadataService
					.updateExecutionOutput(modelResultMetadata, String.format("Error while starting modelExecutor [%s]", 
					modelExecutor.getTag()));
			
			modelResultMetadata = handlePendingExtraction(modelResultMetadata);
			
			throw new ModelExecutionException("Error while invoking START command in ssh environment");
			
		} finally {
			if (connection != null) {
				connection.close();
			}
			modelResultMetadata.setExecutionFinishDate(Calendar.getInstance());
			modelResultMetadataService.update(modelResultMetadata);
		}
	}

	private ModelResultMetadata handlePendingExtraction(ModelResultMetadata modelResultMetadata) {
		if (modelResultMetadata.getExtractorMetadatas() != null && !modelResultMetadata.getExtractorMetadatas().isEmpty()) {
			for (ExtractorMetadata extractorMetadata : modelResultMetadata.getExtractorMetadatas()) {
				extractorMetadata.getModelMetadataExtractor().setExecutionStatus(ExecutionStatus.IDLE);
				extractorMetadata.setModelMetadataExtractor(modelMetadataExtractorService.update(extractorMetadata.getModelMetadataExtractor()));
				
				if (ExecutionStatus.SCHEDULED.equals(extractorMetadata.getExecutionStatus()) || 
						ExecutionStatus.RUNNING.equals(extractorMetadata.getExecutionStatus())) {
					extractorMetadata.setExecutionStatus(ExecutionStatus.ABORTED);
				}
				
				extractorMetadata = extractorMetadataService.update(extractorMetadata);
			}
		}
		
		return modelResultMetadata;
	}
	
	private void handleCloudEnvironmentStartExecution(ExecutionEnvironment executionEnvironment,
			ModelExecutor modelExecutor, List<String> executionExtractors, Boolean uploadMetadata) throws ModelExecutionException {
		
		Connection connection = null;
		ModelResultMetadata modelResultMetadata = setupModelResultMetadata(executionEnvironment, modelExecutor,
				executionExtractors, uploadMetadata);
		
		if (executionEnvironment.getVirtualMachines() == null || 
				executionEnvironment.getVirtualMachines().isEmpty()) {
			throw new ModelExecutionException("No Virtual Machine configurations available");
		}
		try {
			modelResultMetadata.appendSystemLog(String.format("Starting execution of modelExecutor [%s] in Amazon environment...", 
					modelResultMetadata.getModelExecutor().getTag()));
			modelResultMetadata.appendSystemLog(String.format("Setting up Amazon environment [%s]...", 
					executionEnvironment.getTag()));
			modelResultMetadata = modelResultMetadataService.update(modelResultMetadata);
			
			AmazonEC2Client amazonClient = cloudProviderService.authenticateProvider(executionEnvironment);

			String absolutePath = System.getProperty("user.dir");
			cloudProviderService.createCluster(amazonClient, executionEnvironment, absolutePath);

			AmazonMachine amazonMachineInstance = cloudProviderService.getControlInstancesFromCluster(
					amazonClient, executionEnvironment.getClusterName());

			if (amazonMachineInstance == null) {
				modelResultMetadata = modelResultMetadataService
						.updateExecutionOutput(modelResultMetadata, "Control instance was not found");
				throw new ModelExecutionException("Control instance was not found");
                
			} else {
				try {
					modelResultMetadata = modelResultMetadataService
							.updateExecutionOutput(modelResultMetadata, 
									String.format("Starting ssh connection with environment [%s] in Amazon control node [%s]...", 
							executionEnvironment.getTag(), amazonMachineInstance.getPublicDNS()));
					
					log.info("Executing command START in control node [{}]", amazonMachineInstance.getPublicDNS());

	                connection = sshProviderService.openEnvironmentConnection(
	                		amazonMachineInstance.getPublicDNS(), 
	                		executionEnvironment.getUsername(), 
	                		executionEnvironment.getPassword());
	                
	                modelResultMetadata = modelResultMetadataService.updateExecutionOutput(modelResultMetadata, 
	                				String.format("Finished starting ssh connection with environment [%s] in Amazon control node [%s]", 
							executionEnvironment.getTag(), amazonMachineInstance.getPublicDNS()));
	                modelResultMetadata = runInCloud(modelResultMetadata.getModelExecutor(), modelResultMetadata, connection);
                	modelResultMetadata = handleExtractorExecution(connection, executionEnvironment.getComputationalModel(), 
                			modelResultMetadata);
                	modelResultMetadata.setExecutionStatus(ExecutionStatus.FINISHED);
                	
				} catch (AbortedExecutionException e) {
					log.warn("Task was aborted during execution", e);
					modelResultMetadata = modelResultMetadataService.findBySlug(modelResultMetadata.getSlug());
					modelResultMetadata = handlePendingExtraction(modelResultMetadata);
					
				} catch (Exception e) {
					modelResultMetadata.setExecutionStatus(ExecutionStatus.FAILURE);
					modelResultMetadata.setExecutorExecutionStatus(ExecutionStatus.FAILURE);
					modelResultMetadata = modelResultMetadataService
							.updateExecutionOutput(modelResultMetadata, String.format("Error while starting modelExecutor [%s]", 
								modelResultMetadata.getModelExecutor().getTag()));
					
					modelResultMetadata = handlePendingExtraction(modelResultMetadata);
					
					throw new ModelExecutionException("Error while invoking START command in cloud environment");
					
				} finally {
					if (connection != null) {
						connection.close();
					}
					modelResultMetadata.setExecutionFinishDate(Calendar.getInstance());
					modelResultMetadataService.update(modelResultMetadata);
				}
			}
		
		} catch (Exception e) {
			log.error("Error while creating cluster cloud from configuration", e);
		}
	}
	
	private void handleClusterEnvironmentStartExecution(ExecutionEnvironment executionEnvironment,
			ModelExecutor modelExecutor, List<String> executionExtractors, Boolean uploadMetadata) throws Exception {

		Connection connection = null;
		ModelResultMetadata modelResultMetadata = setupModelResultMetadata(executionEnvironment, modelExecutor,
				executionExtractors, uploadMetadata);
		try {
			modelResultMetadata.appendSystemLog(String.format("Starting execution of modelExecutor [%s] in Cluster environment...", 
					modelResultMetadata.getModelExecutor().getTag()));
			modelResultMetadata.appendSystemLog(String.format("Starting ssh connection with Cluster environment [%s]...", 
					executionEnvironment.getTag()));
			modelResultMetadata = modelResultMetadataService.update(modelResultMetadata);
			
			connection = sshProviderService.openEnvironmentConnection(executionEnvironment.getHostAddress(),
					executionEnvironment.getUsername(), executionEnvironment.getPassword());
			
			modelResultMetadata = modelResultMetadataService
					.updateExecutionOutput(modelResultMetadata, String.format("Finished starting ssh connection with Cluster environment [%s]", 
					executionEnvironment.getTag()));
			modelResultMetadata = runInCluster(modelResultMetadata.getModelExecutor(), modelResultMetadata, connection);
			
		} catch (AbortedExecutionException e) {
			log.warn("Task was aborted during execution", e);
			modelResultMetadata = modelResultMetadataService.findBySlug(modelResultMetadata.getSlug());
			modelResultMetadata = handlePendingExtraction(modelResultMetadata);
			
		} catch (Exception e) {
			modelResultMetadata.setExecutionStatus(ExecutionStatus.FAILURE);
			modelResultMetadata = modelResultMetadataService
					.updateExecutionOutput(modelResultMetadata, String.format("Error while starting modelExecutor [%s]", 
						modelResultMetadata.getModelExecutor().getTag()));
			
			modelResultMetadata = handlePendingExtraction(modelResultMetadata);
			
			throw new ModelExecutionException("Error while invoking START command in cluster environment");
			
		} finally {
			if (connection != null) {
				connection.close();
			}
			modelResultMetadata.setExecutionFinishDate(Calendar.getInstance());
			modelResultMetadataService.update(modelResultMetadata);
		}
	}

	private ModelResultMetadata setupModelResultMetadata(ExecutionEnvironment executionEnvironment,
			ModelExecutor modelExecutor, List<String> executionExtractors, Boolean uploadMetadata) {
		modelExecutor.setExecutionStatus(ExecutionStatus.RUNNING);
		modelExecutor = modelExecutorService.update(modelExecutor);
		
		ModelResultMetadata modelResultMetadata = modelResultMetadataService.save(ModelResultMetadata.builder()
			.computationalModel(executionEnvironment.getComputationalModel())
			.modelExecutor(modelExecutor)
			.executionEnvironment(executionEnvironment)
			.userAgent(modelExecutor.getUserAgent())
			.executionStartDate(Calendar.getInstance())
			.executorExecutionStatus(modelExecutor.getExecutionStatus())
			.uploadMetadata(uploadMetadata)
			.build());
		
		modelResultMetadata.setExtractorMetadatas(getExecutionExtractors(executionExtractors , modelResultMetadata));
		modelResultMetadata = modelResultMetadataService.update(modelResultMetadata);
		
		return modelResultMetadata;
	}
	
}	