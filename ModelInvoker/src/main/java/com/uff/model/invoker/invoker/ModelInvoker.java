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
import com.uff.model.invoker.domain.Environment;
import com.uff.model.invoker.domain.ExecutionStatus;
import com.uff.model.invoker.domain.Executor;
import com.uff.model.invoker.domain.Execution;
import com.uff.model.invoker.domain.User;
import com.uff.model.invoker.exception.AbortedExecutionException;
import com.uff.model.invoker.exception.ExecutionException;

import ch.ethz.ssh2.Connection;

@Service
public abstract class ModelInvoker extends ModelKiller {
	
	private static final Logger log = LoggerFactory.getLogger(ModelInvoker.class);
	
	public void startExecutor(Executor executor, Environment environment, User userAgent,
			List<String> executionExtractors, Boolean uploadMetadataToDrive)
			throws RuntimeException, Exception {
		
		if (executor == null) {
			throw new ExecutionException("Executor not found");
		}
		
		if (environment == null) {
			throw new ExecutionException("Environment not found");
		}
		
		Process vpnProcess = vpnProviderService.setupVpnConfigConection(environment.getVpnType(), 
				executor.getComputationalModel().getId(), environment.getVpnConfiguration());
		
		if (EnvironmentType.SSH.equals(environment.getype())) {
			handleSshEnvironmentStartExecution(environment, executor, userAgent, executionExtractors, uploadMetadataToDrive);
			
		} else if (EnvironmentType.CLOUD.equals(environment.getype())) {
			handleCloudEnvironmentStartExecution(environment, executor, userAgent, executionExtractors, uploadMetadataToDrive);
			
		} else if (EnvironmentType.CLUSTER.equals(environment.getype())) {
			handleClusterEnvironmentStartExecution(environment, executor, userAgent, executionExtractors, uploadMetadataToDrive);
		} 
		
		vpnProviderService.closeVpnConnection(vpnProcess);
	}

	private void handleSshEnvironmentStartExecution(Environment environment, Executor executor, User userAgent, 
			List<String> executionExtractorSlugs, Boolean uploadMetadata) throws Exception {
		
		Connection connection = null;
		Execution execution = setupExecution(environment, executor, userAgent, executionExtractorSlugs, uploadMetadata);
		try {
			execution = executionService.updateSystemLog(execution,
					new String[] { String.format("Starting execution of Executor [%s]...", 
							execution.getExecutor().getTag()), 
							String.format("Starting ssh connection with Environment [%s]...", 
									environment.getTag())});
			
			connection = sshProviderService.openEnvironmentConnection(environment.getHostAddress(),
					environment.getUsername(), environment.getPassword());
			
			execution = executionService.updateSystemLog(execution, 
					String.format("Finished setting up ssh connection with Environment [%s]", environment.getTag()));
			
			if (ExecutionStatus.SCHEDULED.equals(execution.getStatus()) || 
					ExecutionStatus.RUNNING.equals(execution.getStatus())) {
				execution = runInSsh(execution.getExecutor(), execution, connection);
				execution.setExecutorStatus(ExecutionStatus.FINISHED);
				execution = executionService.update(execution);
			}

			execution = handleExtractorExecution(connection, environment.getComputationalModel(), execution);
			execution = checkExtractionExecutionStatus(execution);
		
		} catch (AbortedExecutionException e) {
			log.warn("Task was aborted during execution", e);
			execution = handlePendingExtraction(execution);
			
		} catch (Exception e) {
			execution = handleInvokingFailure(executor, execution);
			throw new ExecutionException("Error while invoking START command in ssh Environment", e);
			
		} finally {
			if (connection != null) {
				connection.close();
			}
			execution.setFinishDate(Calendar.getInstance());
			executionService.update(execution);
		}
	}

	private void handleCloudEnvironmentStartExecution(Environment environment, Executor executor, User userAgent, 
			List<String> executionExtractorSlugs, Boolean uploadMetadata) throws ExecutionException {
		
		Connection connection = null;
		Execution execution = setupExecution(environment, executor, userAgent, executionExtractorSlugs, uploadMetadata);
		
		if (environment.getVirtualMachines() == null || 
				environment.getVirtualMachines().isEmpty()) {
			throw new ExecutionException("No Virtual Machine configurations available");
		}
		
		try {
			execution = executionService.updateSystemLog(execution,
					new String[] { String.format("Starting execution of Executor [%s] in Amazon Environment...", 
							execution.getExecutor().getTag()), 
							String.format("Setting up Amazon Environment [%s]...", 
									environment.getTag())});
			
			AmazonEC2Client amazonClient = cloudProviderService.authenticateProvider(environment);
			cloudProviderService.createCluster(amazonClient, environment, Constants.USER_HOME_DIR);
			AmazonMachine amazonMachineInstance = cloudProviderService.getControlInstancesFromCluster(
					amazonClient, environment.getClusterName());

			if (amazonMachineInstance == null) {
				execution = executionService.updateSystemLog(execution, "Control Instance was not found");
				throw new ExecutionException("Control Instance was not found");
                
			} else {
				execution = executionService.updateSystemLog(execution, 
								String.format("Starting ssh connection with Environment [%s] in Amazon Control Node [%s]...", 
						environment.getTag(), amazonMachineInstance.getPublicDNS()));
				
				log.info("Executing command START in Control Node [{}]", amazonMachineInstance.getPublicDNS());

                connection = sshProviderService.openEnvironmentConnection(amazonMachineInstance.getPublicDNS(), 
                		environment.getUsername(), environment.getPassword());
                
                execution = executionService.updateSystemLog(execution, 
                				String.format("Finished starting ssh connection with Environment [%s] in Amazon Control Node [%s]", 
						environment.getTag(), amazonMachineInstance.getPublicDNS()));
                
                if (ExecutionStatus.SCHEDULED.equals(execution.getStatus()) || 
    					ExecutionStatus.RUNNING.equals(execution.getStatus())) {
                	execution = runInCloud(execution.getExecutor(), execution, connection);
                	execution.setExecutorStatus(ExecutionStatus.FINISHED);
                	execution = executionService.update(execution);
                }
            
                execution = handleExtractorExecution(connection, environment.getComputationalModel(), execution);
            	execution = checkExtractionExecutionStatus(execution);
			}
		
		} catch (AbortedExecutionException e) {
			log.warn("Task was aborted during execution", e);
			execution = handlePendingExtraction(execution);
			
		} catch (Exception e) {
			execution = handleInvokingFailure(executor, execution);
			throw new ExecutionException("Error while invoking START command in Cloud Environment", e);
			
		} finally {
			if (connection != null) {
				connection.close();
			}
			execution.setFinishDate(Calendar.getInstance());
			executionService.update(execution);
		}
	}
	
	private void handleClusterEnvironmentStartExecution(Environment environment, Executor executor, User userAgent, 
			List<String> executionExtractors, Boolean uploadMetadata) throws Exception {

		Connection connection = null;
		Execution execution = setupExecution(environment, executor, userAgent, executionExtractors, uploadMetadata);
		try {
			execution = executionService.updateSystemLog(execution,
					new String[] { String.format("Starting execution of Executor [%s] in Cluster Environment...", 
							execution.getExecutor().getTag()), 
							String.format("Starting ssh connection with Cluster Environment [%s]...", 
									environment.getTag())});
			
			connection = sshProviderService.openEnvironmentConnection(environment.getHostAddress(),
					environment.getUsername(), environment.getPassword());
			
			execution = executionService.updateSystemLog(execution, 
					String.format("Finished starting ssh connection with Cluster Environment [%s]", environment.getTag()));
			
			if (ExecutionStatus.SCHEDULED.equals(execution.getStatus()) || 
					ExecutionStatus.RUNNING.equals(execution.getStatus())) {
				execution = runInCluster(execution.getExecutor(), execution, connection);
			}
			
		} catch (AbortedExecutionException e) {
			log.warn("Task was aborted during execution", e);
			execution = handlePendingExtraction(execution);
			
		} catch (Exception e) {
			execution = handleInvokingFailure(executor, execution);
			throw new ExecutionException("Error while invoking START command in Cluster Environment", e);
			
		} finally {
			if (connection != null) {
				connection.close();
			}
			execution.setFinishDate(Calendar.getInstance());
			executionService.update(execution);
		}
	}

	private Execution handleInvokingFailure(Executor executor, Execution execution) {
		execution = executionService.findBySlug(execution.getSlug());
	
		if (!execution.getStatus().equals(ExecutionStatus.ABORTED) && 
				!execution.getExecutorStatus().equals(ExecutionStatus.ABORTED)) {
			execution.setStatus(ExecutionStatus.FAILURE);
			execution.setExecutorStatus(ExecutionStatus.FAILURE);
			execution = executionService.updateSystemLog(execution, String.format("Error while starting Executor [%s]", executor.getTag()));
		}
		
		return handlePendingExtraction(execution);
	}

	private Execution setupExecution(Environment environment, Executor executor, 
			User userAgent, List<String> executionExtractorSlugs, Boolean uploadMetadata) {
		
		Execution execution = executionService.findByExecutorAndEnvironmentAndStatus(executor, environment, ExecutionStatus.RUNNING);
		
		if (execution != null) {
			return execution;
		}
		
		execution = executionService.save(Execution.builder()
			.computationalModel(environment.getComputationalModel())
			.executor(executor)
			.environment(environment)
			.userAgent(userAgent)
			.startDate(Calendar.getInstance())
			.executorStatus(ExecutionStatus.RUNNING)
			.uploadMetadata(uploadMetadata)
			.build());
		
		execution.setExtractorExecutions(getExecutionExtractors(executionExtractorSlugs , execution));
		return executionService.update(execution);
	}
	
}	