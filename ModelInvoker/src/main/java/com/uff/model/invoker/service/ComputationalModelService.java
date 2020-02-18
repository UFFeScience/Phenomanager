package com.uff.model.invoker.service;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.rabbitmq.client.Channel;
import com.uff.model.invoker.Constants;
import com.uff.model.invoker.domain.ComputationalModel;
import com.uff.model.invoker.domain.EnvironmentType;
import com.uff.model.invoker.domain.Environment;
import com.uff.model.invoker.domain.ExecutionStatus;
import com.uff.model.invoker.domain.JobStatus;
import com.uff.model.invoker.domain.Executor;
import com.uff.model.invoker.domain.Extractor;
import com.uff.model.invoker.domain.Execution;
import com.uff.model.invoker.domain.Permission;
import com.uff.model.invoker.domain.User;
import com.uff.model.invoker.domain.dto.amqp.ExecutionMessageDto;
import com.uff.model.invoker.exception.AbortedExecutionException;
import com.uff.model.invoker.invoker.ModelInvoker;
import com.uff.model.invoker.repository.ComputationalModelRepository;
import com.uff.model.invoker.service.core.ApiRestService;
import com.uff.model.invoker.service.provider.ClusterProviderService;
import com.uff.model.invoker.service.provider.SshProviderService;
import com.uff.model.invoker.util.IpAddressValidator;

import ch.ethz.ssh2.Connection;

@Service
public class ComputationalModelService extends ApiRestService<ComputationalModel, ComputationalModelRepository> {

	private static final Logger log = LoggerFactory.getLogger(ComputationalModelService.class);

	@Autowired
	private ComputationalModelRepository computationalModelRepository;
	
	@Autowired
	private BeanFactory beanFactory;
	
	@Autowired
	private PermissionService permissionService;
	
	@Autowired
	private ExecutionService executionService;
	
	@Autowired
	private EnvironmentService environmentService;
	
	@Autowired
	private ExecutorService executorService;
	
	@Autowired
	private ExtractorService extractorService;
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private ClusterProviderService clusterProviderService;
	
	@Autowired
	private SshProviderService sshProviderService;
	
	@Override
	protected ComputationalModelRepository getRepository() {
		return computationalModelRepository;
	}
	
	@Override
	protected Class<ComputationalModel> getEntityClass() {
		return ComputationalModel.class;
	}
	
	public void invokeModelTaskStop(ExecutionMessageDto executionMessageDto, Channel channel, Long tag) throws IOException {
		log.info("Starting stop of Execution of slug [{}]", executionMessageDto.getExecutionSlug());

		Execution execution = executionService.findBySlug(executionMessageDto.getExecutionSlug());
		if (execution == null) {
			log.error("Error while invoking process, Execution of slug [{}] not found", executionMessageDto.getExecutionSlug());
			channel.basicAck(tag, Boolean.TRUE);
			return;
		}
		
		Environment environment = environmentService.findBySlug(executionMessageDto.getEnvironmentSlug());
		if (environment == null || (environment != null && 
				new IpAddressValidator().validateWorkspaceAddress(environment.getHostAddress()))) {
			log.error("Error while invoking process, Environment of slug [{}] not found or has invalid Host Address", 
					executionMessageDto.getEnvironmentSlug());
			channel.basicAck(tag, Boolean.TRUE);
			return;
		}
		
		User user = userService.findBySlug(executionMessageDto.getUserSlug());
		if (user == null) {
			log.error("Error while invoking process, User of slug [{}] not found", executionMessageDto.getUserSlug());
			channel.basicAck(tag, Boolean.TRUE);
			return;
		}
		
		try {
			if (!canAccessComputationalModel(user, execution.getComputationalModel())) {
				log.error("User of slug [{}] doesn't have permission to manage actions on ComputationalModel of slug [{}]", 
						user.getSlug(), execution.getComputationalModel().getSlug());
				channel.basicAck(tag, Boolean.TRUE);
				return;
			}
			
			String strategyBeanName = String.format(Constants.INVOKER_STRATEGY_SUFFIX, 
					execution.getComputationalModel().getType().getTypeName());
			ModelInvoker modelInvokerStrategy = beanFactory.getBean(strategyBeanName, ModelInvoker.class);
			
			if (!execution.getStatus().equals(ExecutionStatus.RUNNING) && 
					!execution.getStatus().equals(ExecutionStatus.SCHEDULED)) {
				log.warn("Error while invoking process, Execution of slug [{}] of ComputationalModel of slug [{}] is not RUNNING nor SCHEDULED", 
						execution.getSlug(), execution.getComputationalModel().getSlug());
				channel.basicAck(tag, Boolean.TRUE);
				return;
			}
			modelInvokerStrategy.stopExecutor(execution, environment);
		
		} catch (Exception e) {
			log.error("Unexpected error while invoking ComputationalModel", e);
			channel.basicAck(tag, Boolean.TRUE);
			return;
		
		}

		channel.basicAck(tag, Boolean.TRUE);
		log.info("Execution Process of ComputationalModel stopped with success for ComputationalModel of slug [{}], currentVersion [{}] and Execution of Slug", 
				execution.getComputationalModel().getSlug(), execution.getComputationalModel().getCurrentVersion(), execution.getSlug());
	}

	public void invokeModelTaskExecutor(ExecutionMessageDto executionMessageDto, Channel channel, Long tag) throws IOException {
		log.info("Starting execution of Executor of slug [{}]", executionMessageDto.getExecutorSlug());

		Executor executor = executorService.findBySlug(executionMessageDto.getExecutorSlug());
		if (executor == null) {
			log.error("Error while invoking process, Executor of slug [{}] not found", executionMessageDto.getExecutorSlug());
			channel.basicAck(tag, Boolean.TRUE);
			return;
		}
		
		Environment environment = environmentService.findBySlug(executionMessageDto.getEnvironmentSlug());
		if (environment == null || (environment != null && 
				new IpAddressValidator().validateWorkspaceAddress(environment.getHostAddress()))) {
			log.error("Error while invoking process, Environment of slug [{}] not found or has invalid Host Address", 
					executionMessageDto.getEnvironmentSlug());
			channel.basicAck(tag, Boolean.TRUE);
			return;
		}
		
		User user = userService.findBySlug(executionMessageDto.getUserSlug());
		if (user == null) {
			log.error("Error while invoking process, User of slug [{}] not found", executionMessageDto.getUserSlug());
			channel.basicAck(tag, Boolean.TRUE);
			return;
		}
		
		try {
			if (!canAccessComputationalModel(user, executor.getComputationalModel())) {
				log.error("User of slug [{}] doesn't have permission to manage actions on ComputationalModel of slug [{}]", 
						user.getSlug(), executor.getComputationalModel().getSlug());
				channel.basicAck(tag, Boolean.TRUE);
				return;
			}
			
			String strategyBeanName = String.format(Constants.INVOKER_STRATEGY_SUFFIX, 
					executor.getComputationalModel().getType().getTypeName());
			ModelInvoker modelInvokerStrategy = beanFactory.getBean(strategyBeanName, ModelInvoker.class);
			modelInvokerStrategy.startExecutor(executor, environment, user,
						executionMessageDto.getExecutionExtractorSlugs(), executionMessageDto.getUploadMetadata());
		
		} catch (Exception e) {
			log.error("Unexpected error while invoking ComputationalModel", e);
			channel.basicAck(tag, Boolean.TRUE);
			return;
		} 

		channel.basicAck(tag, Boolean.TRUE);
		log.info("Execution Process of ComputationalModel started with success for ComputationalModel of slug [{}], currentVersion [{}] and Executor of Slug", 
				executor.getComputationalModel().getSlug(), executor.getComputationalModel().getCurrentVersion(), executor.getSlug());
	}
	
	public void invokeModelTaskExtractor(ExecutionMessageDto executionMessageDto, Channel channel, Long tag) throws IOException {
		log.info("Starting execution of extractor of ComputationalModel of slug [{}] and Extractor of slug [{}]", 
				executionMessageDto.getExtractorSlug());
	
		Extractor extractor = extractorService.findBySlug(executionMessageDto.getExtractorSlug());
		if (extractor == null) {
			log.error("Error while invoking process, Extractor of slug [{}] not found", executionMessageDto.getExecutorSlug());
			channel.basicAck(tag, Boolean.TRUE);
			return;
		}
		
		Environment environment = environmentService.findBySlug(executionMessageDto.getEnvironmentSlug());
		if (environment == null || (environment != null && 
				new IpAddressValidator().validateWorkspaceAddress(environment.getHostAddress()))) {
			log.error("Error while invoking process, Environment of slug [{}] not found or has invalid Host Address", 
					executionMessageDto.getEnvironmentSlug());
			channel.basicAck(tag, Boolean.TRUE);
			return;
		}
		
		User user = userService.findBySlug(executionMessageDto.getUserSlug());
		if (user == null) {
			log.error("Error while invoking process, User of slug [{}] not found", executionMessageDto.getUserSlug());
			channel.basicAck(tag, Boolean.TRUE);
			return;
		}
		
		try {
			if (!canAccessComputationalModel(user, extractor.getComputationalModel())) {
				log.error("User of slug [{}] doesn't have permission to manage actions on ComputationalModel of slug [{}]", 
						user.getSlug(), extractor.getComputationalModel().getSlug());
				channel.basicAck(tag, Boolean.TRUE);
				return;
			}
			
			String strategyBeanName = String.format(Constants.INVOKER_STRATEGY_SUFFIX, 
					extractor.getComputationalModel().getType().getTypeName());
			ModelInvoker modelInvokerStrategy = beanFactory.getBean(strategyBeanName, ModelInvoker.class);
			modelInvokerStrategy.startExtractor(extractor, environment, user,
					executionMessageDto.getUploadMetadata());
		
		} catch (Exception e) {
			log.error("Unexpected error while invoking ComputationalModel extractor", e);
			channel.basicAck(tag, Boolean.TRUE);
			return;
		}
		
		channel.basicAck(tag, Boolean.TRUE);
		log.info("Extraction Process of ComputationalModel started with success for ComputationalModel of slug [{}], currentVersion [{}] and Extractor of Slug", 
				extractor.getComputationalModel().getSlug(), extractor.getComputationalModel().getCurrentVersion(), 
				extractor.getSlug());
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public Boolean canAccessComputationalModel(User userAgent, ComputationalModel computationalModel) {
		log.info("Checking permissions for User of slug [{}] on ComputationalModel of slug [{}]", 
				userAgent.getSlug(), computationalModel.getSlug());
		
		checkPermissionForComputationalModel(userAgent, computationalModel);
		return computationalModel.getPermissionRole() != null;
    }
	
	public void checkPermissionForComputationalModel(User user, ComputationalModel computationalModel) {
		if (user == null || computationalModel == null) {
			return;
		}
		
		Permission userProjectPermission = permissionService.findOneByUserAndEntityNameAndComputationalModelSlug(user, computationalModel.getSlug());
		if (userProjectPermission != null) {
			computationalModel.setPermissionRole(userProjectPermission.getRole());
		}
	}
	
	@Async
	public void updateExecutionStatus() {
		log.info("Starting process of updating execution status on Cluster jobs");
		
		Integer page = 0;
		PageRequest pageRequest = new PageRequest(page, Constants.DEFAULT_LIMIT);
		
		List<Execution> executions = executionService.findByEnvironmentTypeAndStatus(EnvironmentType.CLUSTER, ExecutionStatus.RUNNING, pageRequest);

		while (executions != null && !executions.isEmpty()) {
			for (Execution execution : executions) {
				Connection connection = null;
				
				if (execution.getExecutor() != null) {
					try {
						connection = sshProviderService.openEnvironmentConnection(execution.getEnvironment().getHostAddress(),
								execution.getEnvironment().getUsername(), 
								execution.getEnvironment().getPassword());
						
						JobStatus jobStatus = clusterProviderService.checkJobStatus(connection, execution.getExecutor().getJobName());
	
						if (JobStatus.CANCELLED.equals(jobStatus)) {
							execution.setFinishDate(Calendar.getInstance());
							execution.setStatus(ExecutionStatus.ABORTED);
							execution.setExecutorStatus(ExecutionStatus.ABORTED);
							execution.appendSystemLog(String.format("Finished execution with job status [%s]", jobStatus.name()));
							
						} else if (JobStatus.COMPLETED.equals(jobStatus)) {
							execution.setExecutorStatus(ExecutionStatus.FINISHED);
							execution = executionService.updateSystemLog(execution, 
									String.format("Finished execution with job status [%s]", jobStatus.name()));
							String strategyBeanName = String.format(Constants.INVOKER_STRATEGY_SUFFIX, 
									execution.getComputationalModel().getType().getTypeName());
							ModelInvoker modelInvokerStrategy = beanFactory.getBean(strategyBeanName, ModelInvoker.class);

							try {
								execution = modelInvokerStrategy.handleExtractorExecution(connection, 
										execution.getComputationalModel(), execution);
								execution = modelInvokerStrategy.checkExtractionExecutionStatus(execution);

							} catch (AbortedExecutionException e) {
								log.warn("Task was aborted during execution", e);
								execution = modelInvokerStrategy.handlePendingExtraction(execution);
								
							} catch (Exception e) {
								log.error("Error while extracting metadata of Cluster");
								execution.setStatus(ExecutionStatus.FAILURE);
							}
							
							execution.setFinishDate(Calendar.getInstance());
							
						} else if (JobStatus.FAILED.equals(jobStatus) || JobStatus.NODE_FAIL.equals(jobStatus) ||
								JobStatus.TIMEOUT.equals(jobStatus)) {
							execution.setFinishDate(Calendar.getInstance());
							execution.setStatus(ExecutionStatus.FAILURE);
							execution.setExecutorStatus(ExecutionStatus.FAILURE);
							execution.appendSystemLog(String.format("Finished execution with job status [%s]", jobStatus.name()));
						}
						
					} catch (Exception e) {
						log.error("Error while checking status of job [{}] of ComputationalModel of slug [{}]", 
								execution.getExecutor().getJobName(), execution.getComputationalModel().getSlug(), e);
					} finally {
						if (connection != null) {
							connection.close();
						}
						execution = executionService.update(execution);
					}
				}
			}
			
			page++;
			pageRequest = new PageRequest(page, Constants.DEFAULT_LIMIT);
			executions = executionService.findByEnvironmentTypeAndStatus(EnvironmentType.CLUSTER, ExecutionStatus.RUNNING, pageRequest);
		}
    	log.info("Finished update process of job executions in Cluster");
	}

}