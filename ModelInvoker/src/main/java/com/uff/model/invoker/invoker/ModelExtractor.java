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
import com.uff.model.invoker.domain.Environment;
import com.uff.model.invoker.domain.ExecutionStatus;
import com.uff.model.invoker.domain.ExtractorExecution;
import com.uff.model.invoker.domain.Extractor;
import com.uff.model.invoker.domain.Execution;
import com.uff.model.invoker.domain.User;
import com.uff.model.invoker.domain.api.google.DriveFile;
import com.uff.model.invoker.exception.AbortedExecutionException;
import com.uff.model.invoker.exception.ExecutionException;

import ch.ethz.ssh2.Connection;

@Service
public abstract class ModelExtractor extends BaseInvoker {
	
	private static final Logger log = LoggerFactory.getLogger(ModelExtractor.class);

	public void startExtractor(Extractor extractor, Environment environment, 
			User userAgent, Boolean uploadMetadataToDrive) throws RuntimeException, Exception {
		
		if (extractor == null) {
			throw new ExecutionException("Extractor not found");
		}
		
		if (environment == null) {
			throw new ExecutionException("Environment not found");
		}
		
		Process vpnProcess = vpnProviderService.setupVpnConfigConection(environment.getVpnType(), 
				extractor.getComputationalModel().getId(), environment.getVpnConfiguration());
		
		if (EnvironmentType.SSH.equals(environment.getype())) {
			handleSshEnvironmentStartExtraction(environment, extractor, userAgent, uploadMetadataToDrive);
			
		} else if (EnvironmentType.CLOUD.equals(environment.getype())) {
			handleCloudEnvironmentStartExtraction(environment, extractor, userAgent, uploadMetadataToDrive);
			
		} else if (EnvironmentType.CLUSTER.equals(environment.getype())) {
			handleClusterEnvironmentStartExtraction(environment, extractor, userAgent, uploadMetadataToDrive);
		} 
		
		vpnProviderService.closeVpnConnection(vpnProcess);
	}
	
	private void handleSshEnvironmentStartExtraction(Environment environment,
			Extractor extractor, User userAgent, Boolean uploadMetadata) throws Exception {
		
		Connection connection = null;
		Execution execution = setupExecution(environment, extractor, userAgent, uploadMetadata);
		
		try {
			execution = executionService.updateSystemLog(execution,
					new String[] { String.format("Starting extraction of Extractor [%s]...", 
							extractor.getTag()), 
							String.format("Starting ssh connection with Environment [%s] for extraction...", 
									environment.getTag())});
			
			connection = sshProviderService.openEnvironmentConnection(environment.getHostAddress(),
					environment.getUsername(), environment.getPassword());
			
			execution = executionService.updateSystemLog(execution, String.format("Finished setting up ssh connection with Environment [%s]", 
					environment.getTag()));
			execution = handleExtractorExecution(connection, environment.getComputationalModel(), execution);
			execution = checkExtractionExecutionStatus(execution);
			
		} catch (AbortedExecutionException e) {
			log.warn("Task was aborted during execution", e);
			execution = handlePendingExtraction(execution);
			
		} catch (Exception e) {
			execution = handleExtractingFailure(extractor, execution);
			throw new ExecutionException("Error while invoking Extractor command in ssh environment", e);
			
		} finally {
			if (connection != null) {
				connection.close();
			}
			execution.setFinishDate(Calendar.getInstance());
			execution = executionService.update(execution);
		}
	}
	
	private void handleCloudEnvironmentStartExtraction(Environment environment,
			Extractor extractor, User userAgent, Boolean uploadMetadata) throws ExecutionException {
		
		Connection connection = null;
		Execution execution = setupExecution(environment, extractor, userAgent, uploadMetadata);
		
		if (environment.getVirtualMachines() == null || 
				environment.getVirtualMachines().isEmpty()) {
			throw new ExecutionException("Control Instance was not found");
		}
		try {
			execution = executionService.updateSystemLog(execution,
					new String[] { String.format("Starting extraction of Extractor [%s] in Amazon Environment...", 
							extractor.getTag()), 
							String.format("Setting up Amazon Environment [%s] for extraction...", 
									environment.getTag())});
			
			AmazonEC2Client amazonClient = cloudProviderService.authenticateProvider(environment);
			cloudProviderService.createCluster(amazonClient, environment, Constants.USER_HOME_DIR);
			AmazonMachine amazonMachineInstance = cloudProviderService.getControlInstancesFromCluster(
					amazonClient, environment.getClusterName());

			if (amazonMachineInstance == null) {
				log.warn("Control Instance was not found!");
				execution = executionService.updateSystemLog(execution, "Control Instance was not found");
				return;
                
			} else {
				execution = executionService.updateSystemLog(execution, 
						String.format("Starting ssh connection with Environment [%s] in Amazon control node [%s] for extraction...", 
						environment.getTag(), amazonMachineInstance.getPublicDNS()));
				
				log.info("Executing command START in Control Node [{}]", amazonMachineInstance.getPublicDNS());

                connection = sshProviderService.openEnvironmentConnection(amazonMachineInstance.getPublicDNS(), 
                		environment.getUsername(), environment.getPassword());
                
                execution = handleExtractorExecution(connection, environment.getComputationalModel(), execution);
                execution = checkExtractionExecutionStatus(execution);
			}
		
		} catch (AbortedExecutionException e) {
			log.warn("Task was aborted during execution", e);
			execution = handlePendingExtraction(executionService.findBySlug(execution.getSlug()));
			
		} catch (Exception e) {
			execution = handleExtractingFailure(extractor, execution);
			throw new ExecutionException("Error while invoking START command in Cloud Environment", e);
			
		} finally {
			if (connection != null) {
				connection.close();
			}
			execution.setFinishDate(Calendar.getInstance());
			executionService.update(execution);
		}
	}
	
	private void handleClusterEnvironmentStartExtraction(Environment environment,
			Extractor extractor, User userAgent, Boolean uploadMetadata) throws Exception {

		Connection connection = null;
		Execution execution = setupExecution(environment, extractor, userAgent, uploadMetadata);
		
		try {
			execution = executionService.updateSystemLog(execution,
					new String[] { String.format("Starting extraction of Extractor [%s] in Cluster Environment...", 
							extractor.getTag()), 
							String.format("Starting ssh connection with Cluster Environment [%s]...", 
									environment.getTag())});
			
			connection = sshProviderService.openEnvironmentConnection(environment.getHostAddress(),
					environment.getUsername(), environment.getPassword());
			
            execution = handleExtractorExecution(connection, environment.getComputationalModel(), execution);
            execution = checkExtractionExecutionStatus(execution);
            
		} catch (AbortedExecutionException e) {
			log.warn("Task was aborted during execution", e);
			execution = handlePendingExtraction(executionService.findBySlug(execution.getSlug()));
			
		} catch (Exception e) {
			execution = handleExtractingFailure(extractor, execution);
			throw new ExecutionException("Error while invoking START command in Cluster Environment", e);
			
		} finally {
			if (connection != null) {
				connection.close();
			}
			execution.setFinishDate(Calendar.getInstance());
			executionService.update(execution);
		}
	}

	private Execution handleExtractingFailure(Extractor extractor, Execution execution) {
		execution.setStatus(ExecutionStatus.FAILURE);
		execution = executionService.updateSystemLog(execution, String.format("Error while starting Extractor [%s]", extractor.getTag()));
		return execution;
	}

	private Execution setupExecution(Environment environment, Extractor extractor, User userAgent, Boolean uploadMetadata) {
		
		ExtractorExecution extractorExecution = extractorExecutionService.findByExtractorAndEnvironment(
				extractor, environment, ExecutionStatus.RUNNING);
		
		if (extractorExecution != null && extractorExecution.getExecution() != null) {
			return extractorExecution.getExecution();
		}
		
		Execution execution = executionService.save(Execution.builder()
				.computationalModel(environment.getComputationalModel())
				.userAgent(userAgent)
				.environment(environment)
				.startDate(Calendar.getInstance())
				.uploadMetadata(uploadMetadata)
				.build());

		execution.setExtractorExecutions(getExecutionExtractor(extractor , execution));
		return executionService.update(execution);
	}
	
	public Execution handleExtractorExecution(Connection connection, ComputationalModel computationalModel, 
			Execution execution) throws AbortedExecutionException {

		if (execution.getExtractorExecutions() == null || execution.getExtractorExecutions().isEmpty()) {
			log.warn("No Execution Extractors configured for this execution");
			return execution;
		}
		
		for (ExtractorExecution extractorExecution : execution.getExtractorExecutions()) {
			if (ExecutionStatus.SCHEDULED.equals(extractorExecution.getStatus())) {
			
				if (extractorExecution.getExtractor().getFileId() != null && 
						!"".equals(extractorExecution.getExtractor().getFileId())) {
					try {
						extractorExecution.setStatus(ExecutionStatus.RUNNING);
						extractorExecution = extractorExecutionService.update(extractorExecution);
						
						execution = executionService
								.updateSystemLog(execution, String.format("Downloading Extractor [%s]...", 
								extractorExecution.getExtractor().getTag()));
						
						DriveFile extractorDriveFile = handleFileDownload(extractorExecution.getExtractor().getFileId());
						
						execution = executionService.updateSystemLog(execution,
								new String[] { String.format("Finished downloading Extractor [%s]", 
										extractorExecution.getExtractor().getTag()), 
										String.format("Uploading Extractor [%s] to Environment...", 
												extractorExecution.getExtractor().getTag())});
						
						sshProviderService.sendDataByScp(connection, extractorDriveFile.getFullPath(), Constants.REMOTE_MOUNT_POINT);
						
						String extractionPermissionCommand = getExecutionPermissionCommand(Boolean.FALSE, extractorDriveFile.getFileName(), connection);
						sshProviderService.executeCommand(connection, extractionPermissionCommand);
					
					} catch (AbortedExecutionException e) {
						throw e;
						
					} catch (Exception e) {
						log.error("Error while sending Extractor by scp", e);
						
						execution = executionService
								.updateSystemLog(execution, String.format("Error while sending Extractor [%s] by scp", 
										extractorExecution.getExtractor().getTag()));
						
						extractorExecution.setStatus(ExecutionStatus.FAILURE);
						extractorExecution = extractorExecutionService.update(extractorExecution);
						continue;
					}
				}
				
				try {
					execution = executionService.updateSystemLog(execution, 
							String.format("Starting extraction of metadata with command [%s]...", 
							extractorExecution.getExtractor().getExecutionCommand()));
					
					byte[] executionMetadata = sshProviderService.executeCommand(connection, extractorExecution.getExtractor().getExecutionCommand());
					
					execution = executionService
							.updateSystemLog(execution, String.format("Finished extraction of metadata with the Extractor [%s]", 
									extractorExecution.getExtractor().getTag()));
					try {
						if (executionMetadata != null && executionMetadata.length > 0 && execution.getUploadMetadata() != null && execution.getUploadMetadata()) {
							execution = executionService.updateSystemLog(execution, "Uploading extraction metadata...");
							
							DriveFile driveFile = uploadMetadata(execution.getSlug(), 
									extractorExecution.getExtractor().getTag(), executionMetadata);
							extractorExecution.setExecutionMetadataFileId(driveFile.getFileId());
							
							execution = executionService.updateSystemLog(execution, "Finished uploading extraction metadata");
						}
						
						extractorExecution.setStatus(ExecutionStatus.FINISHED);
				
					} catch (AbortedExecutionException e) {
						throw e;
						
					} catch (Exception e) {
						log.error("Error uploading extracted metadata", e);
						extractorExecution.setStatus(ExecutionStatus.FAILURE);
					}
					extractorExecution = extractorExecutionService.update(extractorExecution);
					
				} catch (IOException | InterruptedException e ) {
					log.error("Error while executing extractor command of Extractor of slug [{}]", extractorExecution.getExtractor().getSlug());
					
					execution = executionService
							.updateSystemLog(execution, String.format("Error while executing Extractor [%s]", 
									extractorExecution.getExtractor().getTag()));
					
					extractorExecution.setStatus(ExecutionStatus.FAILURE);
					extractorExecution = extractorExecutionService.update(extractorExecution);
				} 
			}
		}
		
		return executionService.update(execution);
	}

	public Set<ExtractorExecution> getExecutionExtractors(List<String> executionExtractorSlugs, Execution execution) {
		List<ExtractorExecution> extractorExecutions = new ArrayList<>();
		List<Extractor> extractors = null;
				
		if (executionExtractorSlugs != null && !executionExtractorSlugs.isEmpty()) {
			extractors = extractorService.findAllBySlugInAndActive(executionExtractorSlugs, Boolean.TRUE);
		} else {
			extractors = extractorService.findAllByComputationalModelAndActive(execution.getComputationalModel(), Boolean.TRUE);
		}
		
		if (extractors != null && !extractors.isEmpty()) {
			for (Extractor extractor : extractors) {
				extractorExecutions.add(extractorExecutionService.save(ExtractorExecution.builder()
						.execution(execution)
						.status(ExecutionStatus.SCHEDULED)
						.extractor(extractor)
						.build()));
			}
		}
		
		return new HashSet<>(extractorExecutions);
	}
	
	public Set<ExtractorExecution> getExecutionExtractor(Extractor extractor, Execution execution) {
		List<ExtractorExecution> extractorExecutions = new ArrayList<>();
		
		if (extractor != null ) {
			extractorExecutions.add(extractorExecutionService.save(ExtractorExecution.builder()
					.execution(execution)
					.status(ExecutionStatus.SCHEDULED)
					.extractor(extractor)
					.build()));
		}
		
		return new HashSet<>(extractorExecutions);
	}
	
}	