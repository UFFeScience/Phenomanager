package com.uff.model.invoker.invoker;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.amazonaws.services.ec2.AmazonEC2Client;
import com.uff.model.invoker.Constants;
import com.uff.model.invoker.domain.AmazonMachine;
import com.uff.model.invoker.domain.ComputationalModel;
import com.uff.model.invoker.domain.EnvironmentType;
import com.uff.model.invoker.domain.ExecutionEnvironment;
import com.uff.model.invoker.domain.ExecutionStatus;
import com.uff.model.invoker.domain.ExtractorMetadata;
import com.uff.model.invoker.domain.ModelMetadataExtractor;
import com.uff.model.invoker.domain.ModelResultMetadata;
import com.uff.model.invoker.domain.User;
import com.uff.model.invoker.domain.api.google.DriveFile;
import com.uff.model.invoker.exception.AbortedExecutionException;
import com.uff.model.invoker.exception.ModelExecutionException;

import ch.ethz.ssh2.Connection;

@Service
public abstract class ModelExtractor extends BaseInvoker {
	
	private static final Logger log = LoggerFactory.getLogger(ModelExtractor.class);

	protected static final String REMOTE_MOUNT_POINT = ".";
	
	public void startModelExtractor(ModelMetadataExtractor modelMetadataExtractor, ExecutionEnvironment executionEnvironment, 
			User userAgent, Boolean uploadMetadataToDrive) throws RuntimeException, Exception {
		
		if (modelMetadataExtractor == null) {
			throw new ModelExecutionException("ModelMetadataExtractor not found");
		}
		
		if (executionEnvironment == null) {
			throw new ModelExecutionException("ExecutionEnvironment of not found");
		}
		
		Process vpnProcess = vpnProviderService.setupVpnConfigConection(executionEnvironment.getVpnType(), 
				modelMetadataExtractor.getComputationalModel().getId(), executionEnvironment.getVpnConfiguration());
		
		if (EnvironmentType.SSH.equals(executionEnvironment.getype())) {
			handleSshEnvironmentStartExtraction(executionEnvironment, modelMetadataExtractor, userAgent, uploadMetadataToDrive);
			
		} else if (EnvironmentType.CLOUD.equals(executionEnvironment.getype())) {
			handleCloudEnvironmentStartExtraction(executionEnvironment, modelMetadataExtractor, userAgent, uploadMetadataToDrive);
			
		} else if (EnvironmentType.CLUSTER.equals(executionEnvironment.getype())) {
			handleClusterEnvironmentStartExtraction(executionEnvironment, modelMetadataExtractor, userAgent, uploadMetadataToDrive);
		} 
		
		vpnProviderService.closeVpnConnection(vpnProcess);
	}
	
	private void handleSshEnvironmentStartExtraction(ExecutionEnvironment executionEnvironment,
			ModelMetadataExtractor modelMetadataExtractor, User userAgent, Boolean uploadMetadata) throws Exception {
		
		Connection connection = null;
		ModelResultMetadata modelResultMetadata = setupModelResultMetadata(executionEnvironment, modelMetadataExtractor, userAgent, uploadMetadata);
		
		try {
			modelResultMetadata = modelResultMetadataService.updateSystemLog(modelResultMetadata,
					new String[] { String.format("Starting extraction of modelMetadataExtractor [%s]...", 
							modelMetadataExtractor.getTag()), 
							String.format("Starting ssh connection with environment [%s] for extraction...", 
									executionEnvironment.getTag())});
			
			connection = sshProviderService.openEnvironmentConnection(executionEnvironment.getHostAddress(),
					executionEnvironment.getUsername(), executionEnvironment.getPassword());
			
			modelResultMetadata = modelResultMetadataService
					.updateSystemLog(modelResultMetadata, String.format("Finished setting up ssh connection with environment [%s]", 
					executionEnvironment.getTag()));
			modelResultMetadata = handleExtractorExecution(connection, executionEnvironment.getComputationalModel(), modelResultMetadata);
			modelResultMetadata = checkExecutionExtractionStatus(modelResultMetadata);
			
		} catch (AbortedExecutionException e) {
			log.warn("Task was aborted during execution", e);
			modelResultMetadata = handlePendingExtraction(modelResultMetadata);
			
		} catch (Exception e) {
			modelResultMetadata = handleExtractingFailure(modelMetadataExtractor, modelResultMetadata);
			throw new ModelExecutionException("Error while invoking Extractor command in ssh environment", e);
			
		} finally {
			if (connection != null) {
				connection.close();
			}
			modelResultMetadata.setExecutionFinishDate(Calendar.getInstance());
			modelResultMetadata = modelResultMetadataService.update(modelResultMetadata);
		}
	}
	
	private void handleCloudEnvironmentStartExtraction(ExecutionEnvironment executionEnvironment,
			ModelMetadataExtractor modelMetadataExtractor, User userAgent, Boolean uploadMetadata) throws ModelExecutionException {
		
		Connection connection = null;
		ModelResultMetadata modelResultMetadata = setupModelResultMetadata(executionEnvironment, modelMetadataExtractor, userAgent, uploadMetadata);
		
		if (executionEnvironment.getVirtualMachines() == null || 
				executionEnvironment.getVirtualMachines().isEmpty()) {
			throw new ModelExecutionException("Control instance was not found");
		}
		try {
			modelResultMetadata = modelResultMetadataService.updateSystemLog(modelResultMetadata,
					new String[] { String.format("Starting extraction of modelMetadataExtractor [%s] in Amazon environment...", 
							modelMetadataExtractor.getTag()), 
							String.format("Setting up amazon environment [%s] for extraction...", 
									executionEnvironment.getTag())});
			
			AmazonEC2Client amazonClient = cloudProviderService.authenticateProvider(executionEnvironment);
			cloudProviderService.createCluster(amazonClient, executionEnvironment, Constants.USER_HOME_DIR);
			AmazonMachine amazonMachineInstance = cloudProviderService.getControlInstancesFromCluster(
					amazonClient, executionEnvironment.getClusterName());

			if (amazonMachineInstance == null) {
				log.warn("Control instance was not found!");
				modelResultMetadata = modelResultMetadataService.updateSystemLog(modelResultMetadata, "Control instance was not found");
				return;
                
			} else {
				modelResultMetadata = modelResultMetadataService.updateSystemLog(modelResultMetadata, 
						String.format("Starting ssh connection with environment [%s] in Amazon control node [%s] for extraction...", 
						executionEnvironment.getTag(), amazonMachineInstance.getPublicDNS()));
				
				log.info("Executing command START in control node [{}]", amazonMachineInstance.getPublicDNS());

                connection = sshProviderService.openEnvironmentConnection(amazonMachineInstance.getPublicDNS(), 
                		executionEnvironment.getUsername(), executionEnvironment.getPassword());
                
                modelResultMetadata = handleExtractorExecution(connection, executionEnvironment.getComputationalModel(), modelResultMetadata);
                modelResultMetadata = checkExecutionExtractionStatus(modelResultMetadata);
			}
		
		} catch (AbortedExecutionException e) {
			log.warn("Task was aborted during execution", e);
			modelResultMetadata = handlePendingExtraction(modelResultMetadataService.findBySlug(modelResultMetadata.getSlug()));
			
		} catch (Exception e) {
			modelResultMetadata = handleExtractingFailure(modelMetadataExtractor, modelResultMetadata);
			throw new ModelExecutionException("Error while invoking START command in cloud environment", e);
			
		} finally {
			if (connection != null) {
				connection.close();
			}
			modelResultMetadata.setExecutionFinishDate(Calendar.getInstance());
			modelResultMetadataService.update(modelResultMetadata);
		}
	}
	
	private void handleClusterEnvironmentStartExtraction(ExecutionEnvironment executionEnvironment,
			ModelMetadataExtractor modelMetadataExtractor, User userAgent, Boolean uploadMetadata) throws Exception {

		Connection connection = null;
		ModelResultMetadata modelResultMetadata = setupModelResultMetadata(executionEnvironment, modelMetadataExtractor, userAgent, uploadMetadata);
		
		try {
			modelResultMetadata = modelResultMetadataService.updateSystemLog(modelResultMetadata,
					new String[] { String.format("Starting extraction of modelMetadataExtractor [%s] in Cluster environment...", 
							modelMetadataExtractor.getTag()), 
							String.format("Starting ssh connection with Cluster environment [%s]...", 
									executionEnvironment.getTag())});
			
			connection = sshProviderService.openEnvironmentConnection(executionEnvironment.getHostAddress(),
					executionEnvironment.getUsername(), executionEnvironment.getPassword());
			
            modelResultMetadata = handleExtractorExecution(connection, executionEnvironment.getComputationalModel(), modelResultMetadata);
            modelResultMetadata = checkExecutionExtractionStatus(modelResultMetadata);
            
		} catch (AbortedExecutionException e) {
			log.warn("Task was aborted during execution", e);
			modelResultMetadata = handlePendingExtraction(modelResultMetadataService.findBySlug(modelResultMetadata.getSlug()));
			
		} catch (Exception e) {
			modelResultMetadata = handleExtractingFailure(modelMetadataExtractor, modelResultMetadata);
			throw new ModelExecutionException("Error while invoking START command in cluster environment", e);
			
		} finally {
			if (connection != null) {
				connection.close();
			}
			modelResultMetadata.setExecutionFinishDate(Calendar.getInstance());
			modelResultMetadataService.update(modelResultMetadata);
		}
	}

	private ModelResultMetadata handleExtractingFailure(ModelMetadataExtractor modelMetadataExtractor, ModelResultMetadata modelResultMetadata) {
		modelResultMetadata.setExecutionStatus(ExecutionStatus.FAILURE);
		modelResultMetadata = modelResultMetadataService.updateSystemLog(modelResultMetadata, 
				String.format("Error while starting modelMetadataExtractor [%s]", modelMetadataExtractor.getTag()));
		return modelResultMetadata;
	}

	private ModelResultMetadata setupModelResultMetadata(ExecutionEnvironment executionEnvironment,
			ModelMetadataExtractor modelMetadataExtractor, User userAgent, Boolean uploadMetadata) {
		
		ExtractorMetadata extractorMetadata = extractorMetadataService.findByModelMetadataExtractorAndExecutionEnvironment(
				modelMetadataExtractor, executionEnvironment, ExecutionStatus.RUNNING);
		
		if (extractorMetadata != null && extractorMetadata.getModelResultMetadata() != null) {
			return extractorMetadata.getModelResultMetadata();
		}
		
		ModelResultMetadata modelResultMetadata = modelResultMetadataService.save(ModelResultMetadata.builder()
				.computationalModel(executionEnvironment.getComputationalModel())
				.userAgent(userAgent)
				.executionEnvironment(executionEnvironment)
				.executionStartDate(Calendar.getInstance())
				.uploadMetadata(uploadMetadata)
				.build());

		modelResultMetadata.setExtractorMetadatas(getExecutionExtractor(modelMetadataExtractor , modelResultMetadata));
		return modelResultMetadataService.update(modelResultMetadata);
	}
	
	public ModelResultMetadata handleExtractorExecution(Connection connection, ComputationalModel computationalModel, 
			ModelResultMetadata modelResultMetadata) throws AbortedExecutionException {

		if (modelResultMetadata.getExtractorMetadatas() == null || modelResultMetadata.getExtractorMetadatas().isEmpty()) {
			log.warn("No Execution Extractors configured for this execution");
			return modelResultMetadata;
		}
		
		for (ExtractorMetadata extractorMetadata : modelResultMetadata.getExtractorMetadatas()) {
			if (ExecutionStatus.SCHEDULED.equals(extractorMetadata.getExecutionStatus())) {
			
				if (extractorMetadata.getModelMetadataExtractor().getExtractorFileId() != null && 
						!"".equals(extractorMetadata.getModelMetadataExtractor().getExtractorFileId())) {
					try {
						extractorMetadata.setExecutionStatus(ExecutionStatus.RUNNING);
						extractorMetadata = extractorMetadataService.update(extractorMetadata);
						
						modelResultMetadata = modelResultMetadataService
								.updateSystemLog(modelResultMetadata, String.format("Downloading extractor [%s]...", 
								extractorMetadata.getModelMetadataExtractor().getTag()));
						
						DriveFile extractorDriveFile = handleFileDownload(extractorMetadata.getModelMetadataExtractor().getExtractorFileId());
						
						modelResultMetadata = modelResultMetadataService.updateSystemLog(modelResultMetadata,
								new String[] { String.format("Finished downloading extractor [%s]", 
										extractorMetadata.getModelMetadataExtractor().getTag()), 
										String.format("Uploading extractor [%s] to environment...", 
												extractorMetadata.getModelMetadataExtractor().getTag())});
						
						sshProviderService.sendDataByScp(connection, extractorDriveFile.getFullPath(), REMOTE_MOUNT_POINT);
						
						String extractionPermissionCommand = getExecutionPermissionCommand(Boolean.FALSE, extractorDriveFile.getFileName(), connection);
						sshProviderService.executeCommand(connection, extractionPermissionCommand);
					
					} catch (AbortedExecutionException e) {
						throw e;
						
					} catch (Exception e) {
						log.error("Error while sending extractor by scp", e);
						
						modelResultMetadata = modelResultMetadataService
								.updateSystemLog(modelResultMetadata, String.format("Error while sending extractor [%s] by scp", 
										extractorMetadata.getModelMetadataExtractor().getTag()));
						
						extractorMetadata.setExecutionStatus(ExecutionStatus.FAILURE);
						extractorMetadata = extractorMetadataService.update(extractorMetadata);
						continue;
					}
				}
				
				try {
					modelResultMetadata = modelResultMetadataService.updateSystemLog(modelResultMetadata, 
							String.format("Starting extraction metadata with command [%s]...", 
							extractorMetadata.getModelMetadataExtractor().getExecutionCommand()));
					
					byte[] executionMetadata = sshProviderService.executeCommand(connection, extractorMetadata.getModelMetadataExtractor().getExecutionCommand());
					
					modelResultMetadata = modelResultMetadataService
							.updateSystemLog(modelResultMetadata, String.format("Finished extraction of metadata of the extractor [%s]", 
									extractorMetadata.getModelMetadataExtractor().getTag()));
					try {
						if (executionMetadata != null && executionMetadata.length > 0 && modelResultMetadata.getUploadMetadata() != null && modelResultMetadata.getUploadMetadata()) {
							modelResultMetadata = modelResultMetadataService.updateSystemLog(modelResultMetadata, "Uploading extraction metadata...");
							
							DriveFile driveFile = uploadMetadata(modelResultMetadata.getSlug(), 
									extractorMetadata.getModelMetadataExtractor().getTag(), executionMetadata);
							extractorMetadata.setExecutionMetadataFileId(driveFile.getFileId());
							
							modelResultMetadata = modelResultMetadataService.updateSystemLog(modelResultMetadata, "Finished uploading extraction metadata");
						}
						
						extractorMetadata.setExecutionStatus(ExecutionStatus.FINISHED);
				
					} catch (AbortedExecutionException e) {
						throw e;
						
					} catch (Exception e) {
						log.error("Error uploading extracted metadata", e);
						extractorMetadata.setExecutionStatus(ExecutionStatus.FAILURE);
					}
					extractorMetadata = extractorMetadataService.update(extractorMetadata);
					
				} catch (IOException | InterruptedException e ) {
					log.error("Error while executing extractor command of ModelMetadataExtractor of slug [{}]", 
							extractorMetadata.getModelMetadataExtractor().getSlug());
					
					modelResultMetadata = modelResultMetadataService
							.updateSystemLog(modelResultMetadata, String.format("Error while executing extractor [%s]", 
									extractorMetadata.getModelMetadataExtractor().getTag()));
					
					extractorMetadata.setExecutionStatus(ExecutionStatus.FAILURE);
					extractorMetadata = extractorMetadataService.update(extractorMetadata);
				} 
			}
		}
		
		return modelResultMetadataService.update(modelResultMetadata);
	}

	public Set<ExtractorMetadata> getExecutionExtractors(List<String> executionExtractors, ModelResultMetadata modelResultMetadata) {
		List<ExtractorMetadata> extractorMetadatas = new ArrayList<>();
		List<ModelMetadataExtractor> modelMetadataExtractors = null;
				
		if (executionExtractors != null && !executionExtractors.isEmpty()) {
			modelMetadataExtractors = modelMetadataExtractorService.findAllBySlugInAndActive(executionExtractors, Boolean.TRUE);
		} else {
			modelMetadataExtractors = modelMetadataExtractorService.findAllByComputationalModelAndActive(
					modelResultMetadata.getComputationalModel(), Boolean.TRUE);
		}
		
		if (modelMetadataExtractors != null && !modelMetadataExtractors.isEmpty()) {
			for (ModelMetadataExtractor modelMetadataExtractor : modelMetadataExtractors) {
				extractorMetadatas.add(extractorMetadataService.save(ExtractorMetadata.builder()
						.modelResultMetadata(modelResultMetadata)
						.executionStatus(ExecutionStatus.SCHEDULED)
						.modelMetadataExtractor(modelMetadataExtractor)
						.build()));
			}
		}
		
		return new HashSet<>(extractorMetadatas);
	}
	
	public Set<ExtractorMetadata> getExecutionExtractor(ModelMetadataExtractor modelMetadataExtractor, ModelResultMetadata modelResultMetadata) {
		List<ExtractorMetadata> extractorMetadatas = new ArrayList<>();
		
		if (modelMetadataExtractor != null ) {
			extractorMetadatas.add(extractorMetadataService.save(ExtractorMetadata.builder()
					.modelResultMetadata(modelResultMetadata)
					.executionStatus(ExecutionStatus.SCHEDULED)
					.modelMetadataExtractor(modelMetadataExtractor)
					.build()));
		}
		
		return new HashSet<>(extractorMetadatas);
	}
	
}	