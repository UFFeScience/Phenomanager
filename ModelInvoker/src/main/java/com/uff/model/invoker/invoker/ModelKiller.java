package com.uff.model.invoker.invoker;

import java.io.IOException;
import java.util.Calendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.amazonaws.services.ec2.AmazonEC2Client;
import com.uff.model.invoker.domain.AmazonMachine;
import com.uff.model.invoker.domain.EnvironmentType;
import com.uff.model.invoker.domain.Environment;
import com.uff.model.invoker.domain.ExecutionStatus;
import com.uff.model.invoker.domain.ExtractorExecution;
import com.uff.model.invoker.domain.Execution;
import com.uff.model.invoker.domain.api.google.DriveFile;
import com.uff.model.invoker.exception.GoogleErrorApiException;
import com.uff.model.invoker.exception.ExecutionException;

import ch.ethz.ssh2.Connection;

@Service
public abstract class ModelKiller extends ModelExtractor {
	
	private static final Logger log = LoggerFactory.getLogger(ModelKiller.class);
	
	public void stopExecutor(Execution execution, Environment environment)
			throws RuntimeException, Exception {
		
		if (execution == null) {
			log.warn("Execution not found");
			return;
		}
		
		if (environment == null) {
			log.warn("Environment not found");
			return;
		}
		
		if (ExecutionStatus.ABORTED.equals(execution.getStatus()) ||
				ExecutionStatus.ABORTED.equals(execution.getExecutorStatus())) {
			log.warn("Execution of slug [{}] already aborted", execution.getSlug());
			return;
		}
		
		Process vpnProcess = vpnProviderService.setupVpnConfigConection(environment.getVpnType(), 
				execution.getComputationalModel().getId(), environment.getVpnConfiguration());
		
		if (EnvironmentType.SSH.equals(environment.getype())) {
			handleSshEnvironmentStopExecution(environment, execution);
		
		} else if (EnvironmentType.CLOUD.equals(environment.getype())) {
			handleCloudEnvironmentStopExecution(environment, execution);
		
		} else if (EnvironmentType.CLUSTER.equals(environment.getype())) {
			handleClusterEnvironmentStopExecution(environment, execution);
		} 
		
		vpnProviderService.closeVpnConnection(vpnProcess);
	}
	
	private void handleSshEnvironmentStopExecution(Environment environment, Execution execution) throws Exception {
		Connection connection = null;

		try {
			execution.setHasAbortionRequested(Boolean.TRUE);
			execution = executionService.updateSystemLog(execution,
					new String[] { "Starting abort of execution of Executor [%s]...", 
					String.format("Starting ssh connection with Environment [%s]...", 
							environment.getTag())}, Boolean.TRUE);
			
			connection = sshProviderService.openEnvironmentConnection(environment.getHostAddress(),
					environment.getUsername(), environment.getPassword());
			
			execution = executionService.updateSystemLog(execution, 
					String.format("Finished setting up ssh connection with Environment [%s]", 
					environment.getTag()), Boolean.TRUE);
			
			execution = runStop(execution, connection);
			execution.setExecutorStatus(ExecutionStatus.ABORTED);
			
			execution = killExtraction(execution, connection);
            execution.setStatus(ExecutionStatus.ABORTED);
			
		} catch (Exception e) {
			execution = handleKillingFailure(execution);
			throw new ExecutionException("Error while invoking STOP command in ssh Environment", e);
			
		} finally {
			if (connection != null) {
				connection.close();
			}
			execution.setFinishDate(Calendar.getInstance());
			executionService.update(execution, Boolean.TRUE);
		}
	}
	
	private void handleCloudEnvironmentStopExecution(Environment environment, Execution execution) throws ExecutionException {
		Connection connection = null;
		
		if (environment.getVirtualMachines() == null || 
				environment.getVirtualMachines().isEmpty()) {
			throw new ExecutionException("No Virtual Machine configurations available");
		}
			
		try {
			AmazonEC2Client amazonClient = cloudProviderService.authenticateProvider(environment);
			execution.setHasAbortionRequested(Boolean.TRUE);
			execution = executionService.updateSystemLog(execution,
					new String[] { "Starting abort of execution in Amazon Environment...", 
					String.format("Starting ssh connection with Amazon Environment [%s]...", 
							environment.getTag())}, Boolean.TRUE);
			
			AmazonMachine amazonMachineInstance = cloudProviderService.getControlInstancesFromCluster(
					amazonClient, environment.getClusterName());

			if (amazonMachineInstance == null) {
				execution = executionService.updateSystemLog(execution, "Control Instance was not found", Boolean.TRUE);
				throw new ExecutionException("Control Instance was not found");
			}
			execution = executionService.updateSystemLog(execution, 
					String.format("Starting ssh connection with environment [%s] in Amazon Control Node [%s]...", 
					environment.getTag(), amazonMachineInstance.getPublicDNS()), Boolean.TRUE);
			
			log.info("Executing command STOP in Control Node {}", amazonMachineInstance.getPublicDNS());

            connection = sshProviderService.openEnvironmentConnection(amazonMachineInstance.getPublicDNS(), 
            		environment.getUsername(), environment.getPassword());
         
            execution = executionService.updateSystemLog(execution, 
            		String.format("Finished starting ssh connection with Environment [%s] in Amazon Control Node [%s]", 
					environment.getTag(), amazonMachineInstance.getPublicDNS()), Boolean.TRUE);
           
            execution = runStop(execution, connection);
            execution.setExecutorStatus(ExecutionStatus.ABORTED);
            
            execution = killExtraction(execution, connection);
            execution.setStatus(ExecutionStatus.ABORTED);
		
		} catch (Exception e) {
			execution = handleKillingFailure(execution);
			throw new ExecutionException("Error while invoking STOP command in Cloud Environment", e);
			
		} finally {
			if (connection != null) {
				connection.close();
			}
			execution.setFinishDate(Calendar.getInstance());
			executionService.update(execution, Boolean.TRUE);
		}
	}
	
	private void handleClusterEnvironmentStopExecution(Environment environment, Execution execution) throws Exception {
		Connection connection = null;

		try {
			execution.setHasAbortionRequested(Boolean.TRUE);
			execution = executionService.updateSystemLog(execution,
					new String[] { "Starting abort of execution in Cluster Environment...",
							String.format("Starting ssh connection with Cluster Environment [%s]...", environment.getTag())
					}, Boolean.TRUE);
			
			connection = sshProviderService.openEnvironmentConnection(environment.getHostAddress(),
					environment.getUsername(), environment.getPassword());
			
			execution = executionService.updateSystemLog(execution, 
					String.format("Finished starting ssh connection with Cluster Environment [%s]", environment.getTag()), Boolean.TRUE);
			
			execution = runClusterStop(execution, connection);
			execution = killExtraction(execution, connection);
			
		} catch (Exception e) {
			execution = handleKillingFailure(execution);
			throw new ExecutionException("Error while invoking STOP command in Cluster Environment", e);
			
		} finally {
			if (connection != null) {
				connection.close();
			}
			execution.setFinishDate(Calendar.getInstance());
			executionService.update(execution, Boolean.TRUE);
		}
	}

	private Execution handleKillingFailure(Execution execution) {
		execution.setHasAbortionRequested(Boolean.FALSE);
		execution = executionService.updateSystemLog(execution, "Error while killing execution", Boolean.TRUE);
		return execution;
	}
	
	private Execution runStop(Execution execution, Connection connection) throws IOException, InterruptedException, GoogleErrorApiException {
		if (execution.getExecutorStatus().equals(ExecutionStatus.RUNNING) || 
				execution.getExecutorStatus().equals(ExecutionStatus.SCHEDULED)) {
			
			execution = executionService.updateSystemLog(execution, 
					String.format("Killing execution of Executor with command [%s]...", execution.getExecutor().getAbortionCommand()), Boolean.TRUE);
			
			byte[] abortMetadata = sshProviderService.executeCommand(connection, execution.getExecutor().getAbortionCommand());
			
			if (abortMetadata != null && abortMetadata.length > 0 && execution.getUploadMetadata() != null && execution.getUploadMetadata()) {
				DriveFile driveFile = uploadMetadata(execution.getSlug(), 
						execution.getExecutor().getTag(), abortMetadata);
				execution.setAbortionMetadataFileId(driveFile.getFileId());
			}
			
			execution = executionService.updateSystemLog(execution, 
					String.format("Finished killing execution of Executor [%s]", 
							execution.getExecutor().getTag()), Boolean.TRUE);
			
			execution.setExecutorStatus(ExecutionStatus.ABORTED);
			execution = executionService.update(execution, Boolean.TRUE);
		}
		
		return execution;
	}
	
	private Execution runClusterStop(Execution execution, Connection connection) throws IOException, InterruptedException, GoogleErrorApiException {
		if (execution.getExecutorStatus().equals(ExecutionStatus.RUNNING) || 
				execution.getExecutorStatus().equals(ExecutionStatus.SCHEDULED)) {
			
			execution = executionService.updateSystemLog(execution, 
					String.format("Killing execution job [%s] with command [%s]...", 
							execution.getExecutor().getJobName(), execution.getExecutor().getAbortionCommand()), Boolean.TRUE);
			
			byte[] abortMetadata = clusterProviderService.stopJob(connection, execution.getExecutor().getJobName());
			
			if (abortMetadata != null && abortMetadata.length > 0 && execution.getUploadMetadata() != null && execution.getUploadMetadata()) {
				DriveFile driveFile = uploadMetadata(execution.getSlug(), execution.getExecutor().getTag(), abortMetadata);
				execution.setAbortionMetadataFileId(driveFile.getFileId());
			}
			
			execution = executionService.updateSystemLog(execution, 
					String.format("Finished sending kill command for execution job [%s]", 
							execution.getExecutor().getJobName()), Boolean.TRUE);
		}
		
		return execution;
	}

	private Execution killExtraction(Execution execution, Connection connection) throws IOException, InterruptedException {
		if (execution.getExtractorExecutions() != null && !execution.getExtractorExecutions().isEmpty()) {

			for (ExtractorExecution extractorExecution : execution.getExtractorExecutions()) {
		
				if (extractorExecution.getStatus().equals(ExecutionStatus.RUNNING) || 
						extractorExecution.getStatus().equals(ExecutionStatus.SCHEDULED)) {
					
					execution = executionService
							.updateSystemLog(execution, String.format("Killing extraction of Extractor [%s] with command [%s]...", 
									extractorExecution.getExtractor().getTag(), 
									extractorExecution.getExtractor().getAbortionCommand()), Boolean.TRUE);
					
					sshProviderService.executeCommand(connection, execution.getExecutor().getAbortionCommand());

					execution = executionService
							.updateSystemLog(execution, String.format("Finished killing extraction of Extractor [%s]", 
									extractorExecution.getExtractor().getTag()), Boolean.TRUE);
				}
				extractorExecution = extractorExecutionService.update(extractorExecution);
			}
			execution = executionService.update(execution, Boolean.TRUE);
		}
		
		return execution;
	}
	
}	