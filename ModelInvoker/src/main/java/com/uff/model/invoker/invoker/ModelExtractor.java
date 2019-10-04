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
import com.uff.model.invoker.domain.AmazonMachine;
import com.uff.model.invoker.domain.ComputationalModel;
import com.uff.model.invoker.domain.EnvironmentType;
import com.uff.model.invoker.domain.ExecutionEnvironment;
import com.uff.model.invoker.domain.ExecutionStatus;
import com.uff.model.invoker.domain.ExtractorMetadata;
import com.uff.model.invoker.domain.ModelMetadataExtractor;
import com.uff.model.invoker.domain.ModelResultMetadata;
import com.uff.model.invoker.domain.api.google.DriveFile;
import com.uff.model.invoker.exception.GoogleErrorApiException;
import com.uff.model.invoker.exception.ModelExecutionException;

import ch.ethz.ssh2.Connection;

@Service
public abstract class ModelExtractor extends BaseInvoker {
	
	private static final Logger log = LoggerFactory.getLogger(ModelExtractor.class);

	protected static final String REMOTE_MOUNT_POINT = ".";
	
	public void startModelExtractor(ModelMetadataExtractor modelMetadataExtractor, ExecutionEnvironment executionEnvironment, 
			Boolean uploadMetadataToDrive) throws RuntimeException, Exception {
		
		if (modelMetadataExtractor == null) {
			throw new ModelExecutionException("ModelMetadataExtractor not found");
		}
		
		if (executionEnvironment == null) {
			throw new ModelExecutionException("ExecutionEnvironment of not found");
		}
		
		Process vpnProcess = vpnProviderService.setupVpnConfigConection(executionEnvironment.getVpnType(), 
				modelMetadataExtractor.getComputationalModel().getId(), executionEnvironment.getVpnConfiguration());
		
		if (EnvironmentType.SSH.equals(executionEnvironment.getype())) {
			handleSshEnvironmentStartExtraction(executionEnvironment, modelMetadataExtractor, uploadMetadataToDrive);
			
		} else if (EnvironmentType.CLOUD.equals(executionEnvironment.getype())) {
			handleCloudEnvironmentStartExtraction(executionEnvironment, modelMetadataExtractor, uploadMetadataToDrive);
			
		} else if (EnvironmentType.CLUSTER.equals(executionEnvironment.getype())) {
			handleClusterEnvironmentStartExtraction(executionEnvironment, modelMetadataExtractor, uploadMetadataToDrive);
		} 
		
		vpnProviderService.closeVpnConnection(vpnProcess);
	}
	
	private void handleSshEnvironmentStartExtraction(ExecutionEnvironment executionEnvironment,
			ModelMetadataExtractor modelMetadataExtractor, Boolean uploadMetadata) throws Exception {
		
		Connection connection = null;
		modelMetadataExtractor.setExecutionStatus(ExecutionStatus.RUNNING);
		modelMetadataExtractor = modelMetadataExtractorService.update(modelMetadataExtractor);
		ModelResultMetadata modelResultMetadata = setupModelResultMetadata(executionEnvironment, modelMetadataExtractor, uploadMetadata);
		
		try {
			modelResultMetadata.appendSystemLog(String.format("Starting extraction of modelMetadataExtractor [%s]...", 
					modelMetadataExtractor.getTag()));
			modelResultMetadata.appendSystemLog(String.format("Starting ssh connection with environment [%s] for extraction...", 
					executionEnvironment.getTag()));
			modelResultMetadata = modelResultMetadataService.update(modelResultMetadata);
			
			connection = sshProviderService.openEnvironmentConnection(executionEnvironment.getHostAddress(),
					executionEnvironment.getUsername(), executionEnvironment.getPassword());
			
			modelResultMetadata = modelResultMetadataService
					.updateExecutionOutput(modelResultMetadata, String.format("Finished setting up ssh connection with environment [%s]", 
					executionEnvironment.getTag()));
			
			modelResultMetadata = handleExtractorExecution(connection, executionEnvironment.getComputationalModel(), modelResultMetadata);
			checkExecutionExtractionStatus(modelResultMetadata);
			
		} catch (Exception e) {
			log.error("Error while invoking extractor", e);
			modelResultMetadata.setExecutionStatus(ExecutionStatus.FAILURE);
			
			modelResultMetadata = modelResultMetadataService
					.updateExecutionOutput(modelResultMetadata, String.format("Error while starting modelMetadataExtractor [%s]", 
					modelMetadataExtractor.getTag()));
			
			throw new ModelExecutionException("Error while invoking Extractor command in ssh environment");
			
		} finally {
			if (connection != null) {
				connection.close();
			}
			modelResultMetadata.setExecutionFinishDate(Calendar.getInstance());
			modelResultMetadata = modelResultMetadataService.update(modelResultMetadata);
		}
	}

	private void checkExecutionExtractionStatus(ModelResultMetadata modelResultMetadata) {
		if (modelResultMetadata.getExtractorMetadatas() != null && !modelResultMetadata.getExtractorMetadatas().isEmpty()) {
			Boolean hasFailedExtraction = Boolean.FALSE;
			
			for (ExtractorMetadata extractorMetadata : modelResultMetadata.getExtractorMetadatas()) {
				if (ExecutionStatus.FAILURE.equals(extractorMetadata.getExecutionStatus())) {
					hasFailedExtraction = Boolean.TRUE;
					break;
				}
			}
			
			if (hasFailedExtraction) {
				modelResultMetadata.setExecutionStatus(ExecutionStatus.FAILURE);
			} else {
				modelResultMetadata.setExecutionStatus(ExecutionStatus.FINISHED);
			}
		}
	}
	
	private void handleCloudEnvironmentStartExtraction(ExecutionEnvironment executionEnvironment,
			ModelMetadataExtractor modelMetadataExtractor, Boolean uploadMetadata) throws ModelExecutionException {
		
		Connection connection = null;
		modelMetadataExtractor.setExecutionStatus(ExecutionStatus.RUNNING);
		modelMetadataExtractor = modelMetadataExtractorService.update(modelMetadataExtractor);
		ModelResultMetadata modelResultMetadata = setupModelResultMetadata(executionEnvironment, modelMetadataExtractor, uploadMetadata);
		
		if (executionEnvironment.getVirtualMachines() == null || 
				executionEnvironment.getVirtualMachines().isEmpty()) {
			throw new ModelExecutionException("Control instance was not found");
		}
		try {
			modelResultMetadata.appendSystemLog(String.format("Starting extraction of modelMetadataExtractor [%s] in Amazon environment...", 
					modelMetadataExtractor.getTag()));
			modelResultMetadata.appendSystemLog(String.format("Setting up amazon environment [%s] for extraction...", 
					executionEnvironment.getTag()));
			modelResultMetadata = modelResultMetadataService.update(modelResultMetadata);
			
			AmazonEC2Client amazonClient = cloudProviderService.authenticateProvider(executionEnvironment);

			String absolutePath = System.getProperty("user.dir");
			cloudProviderService.createCluster(amazonClient, executionEnvironment, absolutePath);

			AmazonMachine amazonMachineInstance = cloudProviderService.getControlInstancesFromCluster(
					amazonClient, executionEnvironment.getClusterName());

			if (amazonMachineInstance == null) {
				log.warn("Control instance was not found!");
				modelResultMetadata = modelResultMetadataService.updateExecutionOutput(modelResultMetadata, "Control instance was not found");
				return;
                
			} else {
				try {
					modelResultMetadata = modelResultMetadataService.updateExecutionOutput(modelResultMetadata, 
							String.format("Starting ssh connection with environment [%s] in Amazon control node [%s] for extraction...", 
							executionEnvironment.getTag(), amazonMachineInstance.getPublicDNS()));
					
					log.info("Executing command START in control node [{}]", amazonMachineInstance.getPublicDNS());

	                connection = sshProviderService.openEnvironmentConnection(
	                		amazonMachineInstance.getPublicDNS(), 
	                		executionEnvironment.getUsername(), 
	                		executionEnvironment.getPassword());
	                
	                modelResultMetadata = handleExtractorExecution(connection, executionEnvironment.getComputationalModel(), modelResultMetadata);
	                checkExecutionExtractionStatus(modelResultMetadata);
	                
				} catch (Exception e) {
					log.error("Error while invoking extractor", e);
					modelResultMetadata.setExecutionStatus(ExecutionStatus.FAILURE);
					
					modelResultMetadata = modelResultMetadataService.updateExecutionOutput(modelResultMetadata, 
							String.format("Error while starting modelMetadataExtractor [%s]", 
							modelMetadataExtractor.getTag()));
					
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
	
	private void handleClusterEnvironmentStartExtraction(ExecutionEnvironment executionEnvironment,
			ModelMetadataExtractor modelMetadataExtractor, Boolean uploadMetadata) throws Exception {

		Connection connection = null;
		modelMetadataExtractor.setExecutionStatus(ExecutionStatus.RUNNING);
		modelMetadataExtractor = modelMetadataExtractorService.update(modelMetadataExtractor);
		ModelResultMetadata modelResultMetadata = setupModelResultMetadata(executionEnvironment, modelMetadataExtractor, uploadMetadata);
		
		try {
			modelResultMetadata.appendSystemLog(String.format("Starting extraction of modelMetadataExtractor [%s] in Cluster environment...", 
					modelMetadataExtractor.getTag()));
			modelResultMetadata.appendSystemLog(String.format("Starting ssh connection with Cluster environment [%s]...", 
					executionEnvironment.getTag()));
			
			modelResultMetadata = modelResultMetadataService.update(modelResultMetadata);
			
			connection = sshProviderService.openEnvironmentConnection(executionEnvironment.getHostAddress(),
					executionEnvironment.getUsername(), executionEnvironment.getPassword());
			
            modelResultMetadata = handleExtractorExecution(connection, executionEnvironment.getComputationalModel(), modelResultMetadata);
            checkExecutionExtractionStatus(modelResultMetadata);
            
		} catch (Exception e) {
			log.error("Error while invoking extractor", e);
			modelResultMetadata.setExecutionStatus(ExecutionStatus.FAILURE);
			
			modelResultMetadata = modelResultMetadataService
					.updateExecutionOutput(modelResultMetadata, String.format("Error while starting modelMetadataExtractor [%s]", 
					modelMetadataExtractor.getTag()));
			
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
			ModelMetadataExtractor modelMetadataExtractor, Boolean uploadMetadata) {
		
		ModelResultMetadata modelResultMetadata = modelResultMetadataService.save(ModelResultMetadata.builder()
				.computationalModel(executionEnvironment.getComputationalModel())
				.userAgent(modelMetadataExtractor.getUserAgent())
				.executionEnvironment(executionEnvironment)
				.executionStartDate(Calendar.getInstance())
				.uploadMetadata(uploadMetadata)
				.build());
		modelResultMetadata.setExtractorMetadatas(getExecutionExtractor(modelMetadataExtractor , modelResultMetadata));
		modelResultMetadata = modelResultMetadataService.update(modelResultMetadata);
		
		return modelResultMetadata;
	}
	
	public ModelResultMetadata handleExtractorExecution(Connection connection, ComputationalModel computationalModel, 
			ModelResultMetadata modelResultMetadata) throws IOException, InterruptedException, GoogleErrorApiException {

		if (modelResultMetadata.getExtractorMetadatas() == null || modelResultMetadata.getExtractorMetadatas().isEmpty()) {
			log.warn("No Execution Extractors configured for this execution");
			return modelResultMetadata;
		}
		
		for (ExtractorMetadata extractorMetadata : modelResultMetadata.getExtractorMetadatas()) {
			if (!ExecutionStatus.RUNNING.equals(extractorMetadata.getModelMetadataExtractor().getExecutionStatus())) {
			
				if (extractorMetadata.getModelMetadataExtractor().getExtractorFileId() != null && 
						!"".equals(extractorMetadata.getModelMetadataExtractor().getExtractorFileId())) {
					try {
						if (extractorMetadata.getModelMetadataExtractor().getUserAgent() == null && modelResultMetadata.getModelExecutor() != null) {
							extractorMetadata.getModelMetadataExtractor().setUserAgent(modelResultMetadata.getModelExecutor().getUserAgent());
						}
						extractorMetadata.setExecutionStatus(ExecutionStatus.RUNNING);
						extractorMetadata.getModelMetadataExtractor().setExecutionStatus(ExecutionStatus.RUNNING);
						
						extractorMetadata.setModelMetadataExtractor(modelMetadataExtractorService.update(extractorMetadata.getModelMetadataExtractor()));
						extractorMetadata = extractorMetadataService.update(extractorMetadata);
						
						modelResultMetadata = modelResultMetadataService
								.updateExecutionOutput(modelResultMetadata, String.format("Downloading extractor [%s]...", 
								extractorMetadata.getModelMetadataExtractor().getTag()));
						
						DriveFile extractorDriveFile = handleFileDownload(extractorMetadata.getModelMetadataExtractor().getExtractorFileId());
						
						modelResultMetadata.appendSystemLog(String.format("Finished downloading extractor [%s]", 
								extractorMetadata.getModelMetadataExtractor().getTag()));
						modelResultMetadata.appendSystemLog(String.format("Uploading extractor [%s] to environment...", 
								extractorMetadata.getModelMetadataExtractor().getTag()));
						modelResultMetadata = modelResultMetadataService.update(modelResultMetadata);
						
						sshProviderService.sendDataByScp(connection, extractorDriveFile.getFullPath(), REMOTE_MOUNT_POINT);
						
						String extractionPermissionCommand = getExecutionPermissionCommand(Boolean.FALSE, extractorDriveFile.getFileName(), connection);
						sshProviderService.executeCommand(connection, extractionPermissionCommand);
						
					} catch (Exception e) {
						log.error("Error while sending extractor by scp", e);
						
						modelResultMetadata = modelResultMetadataService
								.updateExecutionOutput(modelResultMetadata, String.format("Error while sending extractor [%s] by scp", 
										extractorMetadata.getModelMetadataExtractor().getTag()));
						
						extractorMetadata.setExecutionStatus(ExecutionStatus.FAILURE);
						extractorMetadata.getModelMetadataExtractor().setExecutionStatus(ExecutionStatus.IDLE);
						
						extractorMetadata.setModelMetadataExtractor(modelMetadataExtractorService.update(extractorMetadata.getModelMetadataExtractor()));
						extractorMetadata = extractorMetadataService.update(extractorMetadata);
						continue;
					}
				}
				
				try {
					modelResultMetadata = modelResultMetadataService.updateExecutionOutput(modelResultMetadata, 
							String.format("Starting extraction metadata with command [%s]...", 
							extractorMetadata.getModelMetadataExtractor().getExecutionCommand()));
					
					byte[] executionMetadata = sshProviderService.executeCommand(connection, extractorMetadata.getModelMetadataExtractor().getExecutionCommand());
					
					modelResultMetadata = modelResultMetadataService
							.updateExecutionOutput(modelResultMetadata, String.format("Finished extraction of metadata of the extractor [%s]", 
									extractorMetadata.getModelMetadataExtractor().getTag()));
					try {
						if (executionMetadata != null && executionMetadata.length > 0 && modelResultMetadata.getUploadMetadata() != null && modelResultMetadata.getUploadMetadata()) {
							modelResultMetadata = modelResultMetadataService.updateExecutionOutput(modelResultMetadata, "Uploading extraction metadata...");
							
							DriveFile driveFile = uploadMetadata(modelResultMetadata.getSlug(), 
									extractorMetadata.getModelMetadataExtractor().getTag(), executionMetadata);
							extractorMetadata.setExecutionMetadataFileId(driveFile.getFileId());
							
							modelResultMetadata = modelResultMetadataService.updateExecutionOutput(modelResultMetadata, "Finished uploading extraction metadata");
						}
						
						extractorMetadata.getModelMetadataExtractor().setExecutionStatus(ExecutionStatus.IDLE);
						extractorMetadata.setExecutionStatus(ExecutionStatus.FINISHED);
						
					} catch (Exception e) {
						log.error("Error uploading extracted metadata", e);
						extractorMetadata.getModelMetadataExtractor().setExecutionStatus(ExecutionStatus.IDLE);
						extractorMetadata.setExecutionStatus(ExecutionStatus.FAILURE);
					}
					
					extractorMetadata.setModelMetadataExtractor(modelMetadataExtractorService.update(extractorMetadata.getModelMetadataExtractor()));
					extractorMetadata = extractorMetadataService.update(extractorMetadata);
					
				} catch (IOException | InterruptedException e ) {
					log.error("Error while executing extractor command of ModelMetadataExtractor of slug [{}]", 
							extractorMetadata.getModelMetadataExtractor().getSlug());
					
					modelResultMetadata = modelResultMetadataService
							.updateExecutionOutput(modelResultMetadata, String.format("Error while executing extractor [%s]", 
									extractorMetadata.getModelMetadataExtractor().getTag()));
					
					extractorMetadata.getModelMetadataExtractor().setExecutionStatus(ExecutionStatus.IDLE);
					extractorMetadata.setExecutionStatus(ExecutionStatus.FAILURE);
					
					extractorMetadata.setModelMetadataExtractor(modelMetadataExtractorService.update(extractorMetadata.getModelMetadataExtractor()));
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
			modelMetadataExtractors = modelMetadataExtractorService.findAllByComputationalModelAndActiveAndExecutionStatusNot(
					modelResultMetadata.getComputationalModel(), Boolean.TRUE, ExecutionStatus.RUNNING);
		}
		
		if (modelMetadataExtractors != null && !modelMetadataExtractors.isEmpty()) {
			for (ModelMetadataExtractor modelMetadataExtractor : modelMetadataExtractors) {
				modelMetadataExtractor.setExecutionStatus(ExecutionStatus.SCHEDULED);
				modelMetadataExtractor = modelMetadataExtractorService.update(modelMetadataExtractor);
				extractorMetadatas.add(extractorMetadataService.save(ExtractorMetadata.builder()
						.modelResultMetadata(modelResultMetadata)
						.executionStatus(modelMetadataExtractor.getExecutionStatus())
						.modelMetadataExtractor(modelMetadataExtractor)
						.build()));
			}
		}
		
		return new HashSet<>(extractorMetadatas);
	}
	
	public Set<ExtractorMetadata> getExecutionExtractor(ModelMetadataExtractor modelMetadataExtractor, ModelResultMetadata modelResultMetadata) {
		List<ExtractorMetadata> extractorMetadatas = new ArrayList<>();
		
		if (modelMetadataExtractor != null ) {
			modelMetadataExtractor.setExecutionStatus(ExecutionStatus.SCHEDULED);
			modelMetadataExtractor = modelMetadataExtractorService.update(modelMetadataExtractor);
			extractorMetadatas.add(extractorMetadataService.save(ExtractorMetadata.builder()
					.modelResultMetadata(modelResultMetadata)
					.executionStatus(modelMetadataExtractor.getExecutionStatus())
					.modelMetadataExtractor(modelMetadataExtractor)
					.build()));
		}
		
		return new HashSet<>(extractorMetadatas);
	}
	
}	