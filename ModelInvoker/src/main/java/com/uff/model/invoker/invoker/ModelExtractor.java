package com.uff.model.invoker.invoker;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;

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
import com.uff.model.invoker.exception.NotFoundApiException;

import ch.ethz.ssh2.Connection;

@Service
public abstract class ModelExtractor extends BaseInvoker {
	
	private static final Logger log = LoggerFactory.getLogger(ModelExtractor.class);

	protected static final String REMOTE_MOUNT_POINT = ".";
	
	public void startModelExtractor(ModelMetadataExtractor modelMetadataExtractor)
			throws RuntimeException, Exception {
		
		if (modelMetadataExtractor == null) {
			throw new ModelExecutionException("ModelMetadataExtractor not found");
		}
		
		ExecutionEnvironment executionEnvironment = executionEnvironmentService
				.findByComputationalModelAndActive(modelMetadataExtractor.getComputationalModel(), Boolean.TRUE);
	
		if (executionEnvironment == null) {
			throw new ModelExecutionException(String.format("ExecutionEnvironment of ComputationalModel of slug [%s], not found", 
					modelMetadataExtractor.getComputationalModel().getSlug()));
		}
		
		Process vpnProcess = vpnProvider.setupVpnConfigConection(executionEnvironment.getVpnType(), 
				modelMetadataExtractor.getComputationalModel().getId(), executionEnvironment.getVpnConfiguration());
		
		if (EnvironmentType.SSH.equals(executionEnvironment.getype())) {
			handleSshEnvironmentStartExtraction(executionEnvironment, modelMetadataExtractor);
			modelMetadataExtractor.setExecutionStatus(ExecutionStatus.FINISHED);
			
		} else if (EnvironmentType.CLOUD.equals(executionEnvironment.getype())) {
			handleCloudEnvironmentStartExtraction(executionEnvironment, modelMetadataExtractor);
			modelMetadataExtractor.setExecutionStatus(ExecutionStatus.FINISHED);
			
		} else if (EnvironmentType.CLUSTER.equals(executionEnvironment.getype())) {
			handleClusterEnvironmentStartExtraction(executionEnvironment, modelMetadataExtractor);
		} 
		
		vpnProvider.closeVpnConnection(vpnProcess);
	}
	
	private void handleSshEnvironmentStartExtraction(ExecutionEnvironment executionEnvironment,
			ModelMetadataExtractor modelMetadataExtractor) throws Exception {
		
		Connection connection = null;
		ModelResultMetadata modelResultMetadata = modelResultMetadataService.save(ModelResultMetadata.builder()
				.computationalModel(executionEnvironment.getComputationalModel())
				.userAgent(modelMetadataExtractor.getUserAgent())
				.executionEnvironment(executionEnvironment)
				.executionStartDate(Calendar.getInstance())
				.build());
		
		try {
			modelResultMetadata.appendExecutionLog(String.format("Starting extraction of modelMetadataExtractor [%s]...", 
					modelMetadataExtractor.getTag()));
			modelResultMetadata.appendExecutionLog(String.format("Starting ssh connection with environment [%s] for extraction...", 
					executionEnvironment.getTag()));
			
			modelResultMetadata = modelResultMetadataService.save(modelResultMetadata);
			
			connection = sshProvider.openEnvironmentConnection(executionEnvironment.getHostAddress(),
					executionEnvironment.getUsername(), executionEnvironment.getPassword());
			
			modelResultMetadata = updateExecutionOutput(modelResultMetadata, String.format("Finished setting up ssh connection with environment [%s]", 
					executionEnvironment.getTag()));
			
			modelResultMetadata = handleExtractorExecution(connection, modelMetadataExtractor, modelResultMetadata);
			
		} catch (Exception e) {
			log.error("Error while invoking extractor", e);
			modelResultMetadata.setExecutionStatus(ExecutionStatus.FAILURE);
			
			modelResultMetadata = updateExecutionOutput(modelResultMetadata, String.format("Error while starting modelMetadataExtractor [%s]", 
					modelMetadataExtractor.getTag()));
			
			modelMetadataExtractor.setExecutionStatus(ExecutionStatus.FAILURE);
			modelMetadataExtractor.setUserAgent(modelResultMetadata.getModelExecutor().getUserAgent());
			modelMetadataExtractor = modelMetadataExtractorService.update(modelMetadataExtractor);
			
			List<ExtractorMetadata> extractorMetadatas = new ArrayList<>();
			extractorMetadatas.add(extractorMetadataService.save(ExtractorMetadata.builder()
					.modelResultMetadata(modelResultMetadata)
					.modelMetadataExtractor(modelMetadataExtractor)
					.build()));
			modelResultMetadata.setExtractorMetadatas(new HashSet<>(extractorMetadatas));
			
			throw new ModelExecutionException("Error while invoking Extractor command in ssh environment");
			
		} finally {
			if (connection != null) {
				connection.close();
			}
			
			modelResultMetadata.setExecutionStatus(ExecutionStatus.FINISHED);
			modelResultMetadata.setExecutionFinishDate(Calendar.getInstance());
			modelResultMetadata = modelResultMetadataService.update(modelResultMetadata);
		}
	}
	
	private void handleCloudEnvironmentStartExtraction(ExecutionEnvironment executionEnvironment,
			ModelMetadataExtractor modelMetadataExtractor) throws ModelExecutionException {
		
		Connection connection = null;
		ModelResultMetadata modelResultMetadata = modelResultMetadataService.save(ModelResultMetadata.builder()
				.computationalModel(executionEnvironment.getComputationalModel())
				.executionEnvironment(executionEnvironment)
				.userAgent(modelMetadataExtractor.getUserAgent())
				.executionStartDate(Calendar.getInstance())
				.build());
		
		if (executionEnvironment.getVirtualMachines() == null || 
				executionEnvironment.getVirtualMachines().isEmpty()) {
			throw new ModelExecutionException("Control instance was not found");
		}
			
		try {
			modelResultMetadata.appendExecutionLog(String.format("Starting extraction of modelMetadataExtractor [%s] in Amazon environment...", 
					modelMetadataExtractor.getTag()));
			modelResultMetadata.appendExecutionLog(String.format("Setting up amazon environment [%s] for extraction...", 
					executionEnvironment.getTag()));
			
			modelResultMetadata = modelResultMetadataService.save(modelResultMetadata);
			
			AmazonEC2Client amazonClient = cloudProvider.authenticateProvider(executionEnvironment);

			String absolutePath = System.getProperty("user.dir");
			cloudProvider.createCluster(amazonClient, executionEnvironment, absolutePath);

			AmazonMachine amazonMachineInstance = cloudProvider.getControlInstancesFromCluster(
					amazonClient, executionEnvironment.getClusterName());

			if (amazonMachineInstance == null) {
				log.warn("Control instance was not found!");
				modelResultMetadata = updateExecutionOutput(modelResultMetadata, "Control instance was not found");
				return;
                
			} else {
				try {
					modelResultMetadata = updateExecutionOutput(modelResultMetadata, 
							String.format("Starting ssh connection with environment [%s] in Amazon control node [%s] for extraction...", 
							executionEnvironment.getTag(), amazonMachineInstance.getPublicDNS()));
					
					log.info("Executing command START in control node [{}]", amazonMachineInstance.getPublicDNS());

	                connection = sshProvider.openEnvironmentConnection(
	                		amazonMachineInstance.getPublicDNS(), 
	                		executionEnvironment.getUsername(), 
	                		executionEnvironment.getPassword());
	                
	                modelResultMetadata = handleExtractorExecution(connection, modelMetadataExtractor, modelResultMetadata);
	                
				} catch (Exception e) {
					log.error("Error while invoking extractor", e);
					modelResultMetadata.setExecutionStatus(ExecutionStatus.FAILURE);
					
					modelResultMetadata = updateExecutionOutput(modelResultMetadata, 
							String.format("Error while starting modelMetadataExtractor [%s]", 
							modelMetadataExtractor.getTag()));
					
					modelMetadataExtractor.setExecutionStatus(ExecutionStatus.FAILURE);
					modelMetadataExtractor.setUserAgent(modelResultMetadata.getModelExecutor().getUserAgent());
					modelMetadataExtractor = modelMetadataExtractorService.update(modelMetadataExtractor);
					
					List<ExtractorMetadata> extractorMetadatas = new ArrayList<>();
					extractorMetadatas.add(extractorMetadataService.save(ExtractorMetadata.builder()
							.modelResultMetadata(modelResultMetadata)
							.modelMetadataExtractor(modelMetadataExtractor)
							.build()));
					modelResultMetadata.setExtractorMetadatas(new HashSet<>(extractorMetadatas));
					
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
	
	private void handleClusterEnvironmentStartExtraction(ExecutionEnvironment executionEnvironment,
			ModelMetadataExtractor modelMetadataExtractor) throws Exception {

		Connection connection = null;
		ModelResultMetadata modelResultMetadata = modelResultMetadataService.save(ModelResultMetadata.builder()
				.computationalModel(executionEnvironment.getComputationalModel())
				.userAgent(modelMetadataExtractor.getUserAgent())
				.executionEnvironment(executionEnvironment)
				.executionStartDate(Calendar.getInstance())
				.build());
		
		try {
			modelResultMetadata.appendExecutionLog(String.format("Starting extraction of modelMetadataExtractor [%s] in Cluster environment...", 
					modelMetadataExtractor.getTag()));
			modelResultMetadata.appendExecutionLog(String.format("Starting ssh connection with Cluster environment [%s]...", 
					executionEnvironment.getTag()));
			
			modelResultMetadata = modelResultMetadataService.save(modelResultMetadata);
			
			connection = sshProvider.openEnvironmentConnection(executionEnvironment.getHostAddress(),
					executionEnvironment.getUsername(), executionEnvironment.getPassword());
			
            modelResultMetadata = handleExtractorExecution(connection, modelMetadataExtractor, modelResultMetadata);

		} catch (Exception e) {
			log.error("Error while invoking extractor", e);
			modelResultMetadata.setExecutionStatus(ExecutionStatus.FAILURE);
			
			modelMetadataExtractor.setExecutionStatus(ExecutionStatus.FAILURE);
			modelMetadataExtractor.setUserAgent(modelResultMetadata.getModelExecutor().getUserAgent());
			modelMetadataExtractor = modelMetadataExtractorService.update(modelMetadataExtractor);
			
			List<ExtractorMetadata> extractorMetadatas = new ArrayList<>();
			extractorMetadatas.add(extractorMetadataService.save(ExtractorMetadata.builder()
					.modelResultMetadata(modelResultMetadata)
					.modelMetadataExtractor(modelMetadataExtractor)
					.build()));
			modelResultMetadata.setExtractorMetadatas(new HashSet<>(extractorMetadatas));
			
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
	
	protected ModelResultMetadata handleExtractorExecution(Connection connection, ModelMetadataExtractor modelMetadataExtractor, 
			ModelResultMetadata modelResultMetadata) throws IOException, InterruptedException, NotFoundApiException, GoogleErrorApiException {
		
		if (modelMetadataExtractor == null) {
			log.info("No ModelMetadataExtractor configured");
		}
		
		List<ExtractorMetadata> extractorMetadatas = new ArrayList<>();
		
		if (modelMetadataExtractor.getExtractorFileId() != null && !"".equals(modelMetadataExtractor.getExtractorFileId())) {
			modelResultMetadata = updateExecutionOutput(modelResultMetadata, String.format("Downloading extractor [%s]...", 
					modelMetadataExtractor.getTag()));
			
			String fileTempName = handleFileDownload(modelMetadataExtractor.getExtractorFileId(), "executor-" + 
					modelMetadataExtractor.getComputationalModel().getId());
			
			modelResultMetadata.appendExecutionLog(String.format("Finished downloading extractor [%s]", 
					modelMetadataExtractor.getTag()));
			modelResultMetadata.appendExecutionLog(String.format("Uploading extractor [%s] to environment...", 
					modelMetadataExtractor.getTag()));
			
			modelResultMetadata = modelResultMetadataService.save(modelResultMetadata);
			
			sshProvider.sendDataByScp(connection, fileTempName, REMOTE_MOUNT_POINT);
			
			modelResultMetadata = updateExecutionOutput(modelResultMetadata, String.format("Finished uploading extractor [%s] to environment", 
					modelMetadataExtractor.getTag()));
		}
			
		ExtractorMetadata extractorMetadata = collectExtractorMetadata(connection, modelResultMetadata, modelMetadataExtractor);
		if (extractorMetadata != null) {
			extractorMetadatas.add(extractorMetadata);
		}
		
		modelResultMetadata.setExtractorMetadatas(new HashSet<>(extractorMetadatas));
		return modelResultMetadataService.update(modelResultMetadata);
	}
	
	protected ModelResultMetadata handleExtractorExecution(Connection connection, ComputationalModel computationalModel, 
			ModelResultMetadata modelResultMetadata) throws IOException, InterruptedException, GoogleErrorApiException {
		
		List<ModelMetadataExtractor> modelMetadataExtractors = modelMetadataExtractorService.findAllByComputationalModelAndActive(
				computationalModel, Boolean.TRUE);
		if (modelMetadataExtractors == null || modelMetadataExtractors.isEmpty()) {
			log.info("No ModelMetadataExtractor configured");
			return modelResultMetadata;
		}
		
		List<ExtractorMetadata> extractorMetadatas = new ArrayList<>();
		
		for (ModelMetadataExtractor modelMetadataExtractor : modelMetadataExtractors) {
			if (modelMetadataExtractor.getExtractorFileId() != null && !"".equals(modelMetadataExtractor.getExtractorFileId())) {
				try {
					modelResultMetadata = updateExecutionOutput(modelResultMetadata, String.format("Downloading extractor [%s]...", 
							modelMetadataExtractor.getTag()));
					
					String fileTempName = handleFileDownload(modelMetadataExtractor.getExtractorFileId(), "executor-" + 
							modelMetadataExtractor.getComputationalModel().getId());
					
					modelResultMetadata.appendExecutionLog(String.format("Finished downloading extractor [%s]", 
							modelMetadataExtractor.getTag()));
					modelResultMetadata.appendExecutionLog(String.format("Uploading extractor [%s] to environment...", 
							modelMetadataExtractor.getTag()));
					
					modelResultMetadata = modelResultMetadataService.save(modelResultMetadata);
					
					sshProvider.sendDataByScp(connection, fileTempName, REMOTE_MOUNT_POINT);
					
				} catch (Exception e) {
					log.error("Error while sending extractor by scp", e);
					
					modelResultMetadata = updateExecutionOutput(modelResultMetadata, String.format("Error while sending extractor [%s] by scp", 
							modelMetadataExtractor.getTag()));
					
					modelMetadataExtractor.setExecutionStatus(ExecutionStatus.FAILURE);
					modelMetadataExtractor.setUserAgent(modelResultMetadata.getModelExecutor().getUserAgent());
					modelMetadataExtractor = modelMetadataExtractorService.update(modelMetadataExtractor);
					
					extractorMetadatas.add(extractorMetadataService.save(ExtractorMetadata.builder()
						.modelResultMetadata(modelResultMetadata)
						.modelMetadataExtractor(modelMetadataExtractor)
						.build()));
					continue;
				}
			}
			
			modelMetadataExtractor.setExecutionStatus(ExecutionStatus.RUNNING);
			modelMetadataExtractor.setUserAgent(modelResultMetadata.getModelExecutor().getUserAgent());
			modelMetadataExtractor = modelMetadataExtractorService.update(modelMetadataExtractor);
			
			ExtractorMetadata extractorMetadata = collectExtractorMetadata(connection, modelResultMetadata, modelMetadataExtractor);
			if (extractorMetadata != null) {
				extractorMetadatas.add(extractorMetadata);
			}
			
			if (!ExecutionStatus.FAILURE.equals(modelMetadataExtractor.getExecutionStatus())) {
				modelMetadataExtractor.setExecutionStatus(ExecutionStatus.FINISHED);
			}
			
			modelMetadataExtractorService.update(modelMetadataExtractor);
		}
		
		modelResultMetadata.setExtractorMetadatas(new HashSet<>(extractorMetadatas));
		
		return modelResultMetadataService.update(modelResultMetadata);
	}
	
	private ExtractorMetadata collectExtractorMetadata(Connection connection, ModelResultMetadata modelResultMetadata,
			ModelMetadataExtractor modelMetadataExtractor) throws IOException, GoogleErrorApiException {

		ExtractorMetadata extractorMetadata = extractorMetadataService.save(ExtractorMetadata.builder()
				.modelResultMetadata(modelResultMetadata)
				.modelMetadataExtractor(modelMetadataExtractor)
				.build());
		byte[] executionMetadata = null;
		
		try {
			modelResultMetadata = updateExecutionOutput(modelResultMetadata, String.format("Starting extraction metadata with command [%s]...", 
					modelMetadataExtractor.getExecutionCommand()));
			
			executionMetadata = sshProvider.executeCommand(connection, modelMetadataExtractor.getExecutionCommand());
			
			modelResultMetadata = updateExecutionOutput(modelResultMetadata, String.format("Finished extraction of metadata of the extractor [%s]", 
					modelMetadataExtractor.getTag()));
			
			extractorMetadata.setExecutionStatus(ExecutionStatus.FINISHED);
			
		} catch (IOException | InterruptedException e ) {
			log.error("Error while executing extractor command of ModelMetadataExtractor of slug [{}]", modelMetadataExtractor.getSlug());
			
			modelResultMetadata = updateExecutionOutput(modelResultMetadata, String.format("Error while executing extractor [%s]", 
					modelMetadataExtractor.getTag()));
			
			modelMetadataExtractor.setExecutionStatus(ExecutionStatus.FAILURE);
			extractorMetadata.setExecutionStatus(ExecutionStatus.FAILURE);
		} 

		if (executionMetadata != null) {
			DriveFile driveFile = uploadMetadata(modelResultMetadata.getSlug(), modelMetadataExtractor.getTag(), executionMetadata);
			extractorMetadata.setExecutionMetadataFileId(driveFile.getFileId());
		}
		
		return extractorMetadataService.update(extractorMetadata);
	}
	
}	