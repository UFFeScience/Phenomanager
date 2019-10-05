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
import com.uff.model.invoker.domain.ExecutionCommand;
import com.uff.model.invoker.domain.ExecutionEnvironment;
import com.uff.model.invoker.domain.ExecutionStatus;
import com.uff.model.invoker.domain.JobStatus;
import com.uff.model.invoker.domain.ModelExecutor;
import com.uff.model.invoker.domain.ModelMetadataExtractor;
import com.uff.model.invoker.domain.ModelResultMetadata;
import com.uff.model.invoker.domain.Permission;
import com.uff.model.invoker.domain.User;
import com.uff.model.invoker.domain.dto.amqp.ModelExecutionMessageDto;
import com.uff.model.invoker.invoker.ModelInvoker;
import com.uff.model.invoker.repository.ComputationalModelRepository;
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
	private ModelResultMetadataService modelResultMetadataService;
	
	@Autowired
	private ExecutionEnvironmentService executionEnvironmentService;
	
	@Autowired
	private ModelExecutorService modelExecutorService;
	
	@Autowired
	private ModelMetadataExtractorService modelMetadataExtractorService;
	
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
	
	public void invokeModelTaskExecutor(ModelExecutionMessageDto modelExecutionMessageDto, Channel channel, Long tag) throws IOException {
		log.info("Starting execution of ModelExecutor of slug [{}]", modelExecutionMessageDto.getModelExecutorSlug());

		ModelExecutor modelExecutor = modelExecutorService.findBySlug(modelExecutionMessageDto.getModelExecutorSlug());
		if (modelExecutor == null) {
			log.error("Error while invoking process, ModelExecutor of slug [{}] not found", modelExecutionMessageDto.getModelExecutorSlug());
			channel.basicAck(tag, Boolean.TRUE);
			return;
		}
		
		ExecutionEnvironment executionEnvironment = executionEnvironmentService.findBySlug(
				modelExecutionMessageDto.getExecutionEnvironmentSlug());
		if (executionEnvironment == null || (executionEnvironment != null && 
				new IpAddressValidator().validateWorkspaceAddress(executionEnvironment.getHostAddress()))) {
			log.error("Error while invoking process, ExecutionEnvironment of slug [{}] not found or has invalid Host Address", 
					modelExecutionMessageDto.getExecutionEnvironmentSlug());
			modelExecutor.setExecutionStatus(ExecutionStatus.IDLE);
			modelExecutorService.update(modelExecutor);
			channel.basicAck(tag, Boolean.TRUE);
			return;
		}
		
		User user = userService.findBySlug(modelExecutionMessageDto.getUserSlug());
		if (user == null) {
			log.error("Error while invoking process, User of slug [{}] not found", modelExecutionMessageDto.getUserSlug());
			modelExecutor.setExecutionStatus(ExecutionStatus.IDLE);
			modelExecutorService.update(modelExecutor);
			channel.basicAck(tag, Boolean.TRUE);
			return;
		}
		modelExecutor.setUserAgent(user);
		
		try {
			if (!canAccessComputationalModel(user, modelExecutor.getComputationalModel())) {
				log.error("User of slug [{}] doesn't have permission to manage actions on ComputationalModel of slug [{}]", 
						user.getSlug(), modelExecutor.getComputationalModel().getSlug());
				modelExecutor.setExecutionStatus(ExecutionStatus.IDLE);
				modelExecutorService.update(modelExecutor);
				channel.basicAck(tag, Boolean.TRUE);
				return;
			}
			
			String strategyBeanName = String.format("%sInvokerStrategy", 
					modelExecutor.getComputationalModel().getType().getTypeName());
			ModelInvoker modelInvokerStrategy = beanFactory.getBean(strategyBeanName, ModelInvoker.class);
			
			if (ExecutionCommand.START.equals(modelExecutionMessageDto.getExecutionCommand())) {
				if (modelExecutor.getExecutionStatus().equals(ExecutionStatus.RUNNING)) {
					log.warn("Error while invoking process, ModelExecutor of slug [{}] of ComputationalModel of slug [{}] is already RUNNING", 
							modelExecutor.getSlug(), modelExecutor.getComputationalModel().getSlug());
					channel.basicAck(tag, Boolean.TRUE);
					return;
				}
				modelInvokerStrategy.startModelExecutor(modelExecutor, executionEnvironment, 
						modelExecutionMessageDto.getExecutionExtractors(), modelExecutionMessageDto.getUploadMetadata());
				
			} else if (ExecutionCommand.STOP.equals(modelExecutionMessageDto.getExecutionCommand())) {
				if (modelExecutor.getExecutionStatus().equals(ExecutionStatus.RUNNING)) {
					log.warn("Error while invoking process, ComputationalModel of slug [{}] is not RUNNING, so it can't be stopped", 
							modelExecutor.getComputationalModel().getSlug());
					channel.basicAck(tag, Boolean.TRUE);
					return;
				}
				modelInvokerStrategy.stopModelExecutor(modelExecutor, executionEnvironment);
				
			} else {
				log.warn("Unexpected command [{}] for ComputationalModel of slug [{}]", 
						modelExecutionMessageDto.getExecutionCommand(), modelExecutor.getComputationalModel().getSlug());
				modelExecutor.setExecutionStatus(ExecutionStatus.IDLE);
				modelExecutorService.update(modelExecutor);
			}
		
		} catch (Exception e) {
			log.error("Unexpected error while invoking ComputationalModel", e);
			channel.basicAck(tag, Boolean.TRUE);
			return;
		
		} finally {
			modelExecutor.setExecutionStatus(ExecutionStatus.IDLE);
			modelExecutorService.update(modelExecutor);
		}

		channel.basicAck(tag, Boolean.TRUE);
		log.info("Execution Process of ComputationalModel started with success for ComputationalModel of slug [{}], currentVersion [{}] and ModelExecutor of Slug", 
				modelExecutor.getComputationalModel().getSlug(), modelExecutor.getComputationalModel().getCurrentVersion(), modelExecutor.getSlug());
	}
	
	public void invokeModelTaskExtractor(ModelExecutionMessageDto modelExecutionMessageDto, Channel channel, Long tag) throws IOException {
		log.info("Starting execution of extractor of ComputationalModel of slug [{}] and ModelMetadataExtractor of slug [{}]", 
				modelExecutionMessageDto.getModelMetadataExtractorSlug());
	
		ModelMetadataExtractor modelMetadataExtractor = modelMetadataExtractorService.findBySlug(
				modelExecutionMessageDto.getModelMetadataExtractorSlug());
		if (modelMetadataExtractor == null) {
			log.error("Error while invoking process, ModelMetadataExtractor of slug [{}] not found", modelExecutionMessageDto.getModelExecutorSlug());
			channel.basicAck(tag, Boolean.TRUE);
			return;
		}
		
		if (modelMetadataExtractor.getExecutionStatus().equals(ExecutionStatus.RUNNING)) {
			log.error("Error while invoking process, the ModelMetadataExtractor of slug [{}] of ComputationalModel of slug [{}] is already RUNNING", 
					modelMetadataExtractor.getSlug(), modelMetadataExtractor.getComputationalModel().getSlug());
			channel.basicAck(tag, Boolean.TRUE);
			return;
		}
		
		ExecutionEnvironment executionEnvironment = executionEnvironmentService.findBySlug(
				modelExecutionMessageDto.getExecutionEnvironmentSlug());
		if (executionEnvironment == null || (executionEnvironment != null && 
				new IpAddressValidator().validateWorkspaceAddress(executionEnvironment.getHostAddress()))) {
			log.error("Error while invoking process, ExecutionEnvironment of slug [{}] not found or has invalid Host Address", 
					modelExecutionMessageDto.getExecutionEnvironmentSlug());
			modelMetadataExtractor.setExecutionStatus(ExecutionStatus.IDLE);
			modelMetadataExtractorService.update(modelMetadataExtractor);
			channel.basicAck(tag, Boolean.TRUE);
			return;
		}
		
		User user = userService.findBySlug(modelExecutionMessageDto.getUserSlug());
		if (user == null) {
			log.error("Error while invoking process, User of slug [{}] not found", modelExecutionMessageDto.getUserSlug());
			modelMetadataExtractor.setExecutionStatus(ExecutionStatus.IDLE);
			modelMetadataExtractorService.update(modelMetadataExtractor);
			channel.basicAck(tag, Boolean.TRUE);
			return;
		}
		modelMetadataExtractor.setUserAgent(user);
		
		try {
			if (!canAccessComputationalModel(user, modelMetadataExtractor.getComputationalModel())) {
				log.error("User of slug [{}] doesn't have permission to manage actions on ComputationalModel of slug [{}]", 
						user.getSlug(), modelMetadataExtractor.getComputationalModel().getSlug());
				modelMetadataExtractor.setExecutionStatus(ExecutionStatus.IDLE);
				modelMetadataExtractorService.update(modelMetadataExtractor);
				channel.basicAck(tag, Boolean.TRUE);
				return;
			}
			
			String strategyBeanName = String.format("%sInvokerStrategy", 
					modelMetadataExtractor.getComputationalModel().getType().getTypeName());
			ModelInvoker modelInvokerStrategy = beanFactory.getBean(strategyBeanName, ModelInvoker.class);
			modelInvokerStrategy.startModelExtractor(modelMetadataExtractor, executionEnvironment, 
					modelExecutionMessageDto.getUploadMetadata());
		
		} catch (Exception e) {
			log.error("Unexpected error while invoking ComputationalModel extractor", e);
			modelMetadataExtractor.setExecutionStatus(ExecutionStatus.IDLE);
			modelMetadataExtractorService.update(modelMetadataExtractor);
			channel.basicAck(tag, Boolean.TRUE);
			return;
		}
		
		channel.basicAck(tag, Boolean.TRUE);
		log.info("Extraction Process of ComputationalModel started with success for ComputationalModel of slug [{}], currentVersion [{}] and ModelMetadataExtractor of Slug", 
				modelMetadataExtractor.getComputationalModel().getSlug(), modelMetadataExtractor.getComputationalModel().getCurrentVersion(), 
				modelMetadataExtractor.getSlug());
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
		
		Permission userProjectPermission = permissionService.findOneByUserAndEntityNameAndComputationalModelSlug(
				user, computationalModel.getSlug());
		if (userProjectPermission != null) {
			computationalModel.setPermissionRole(userProjectPermission.getRole());
		}
	}
	
	@Async
	public void updateModelExecutionStatus() {
		log.info("Starting process of updating execution status on Cluster jobs");
		
		Integer page = 0;
		PageRequest pageRequest = new PageRequest(page, Constants.DEFAULT_LIMIT);
		
		List<ModelResultMetadata> modelResultMetadatas = modelResultMetadataService
				.findByExecutionEnvironmentTypeAndExecutionStatus(
						EnvironmentType.CLUSTER, ExecutionStatus.RUNNING, pageRequest);

		while (modelResultMetadatas != null && !modelResultMetadatas.isEmpty()) {
			for (ModelResultMetadata modelResultMetadata : modelResultMetadatas) {
				Connection connection = null;
				
				if (modelResultMetadata.getModelExecutor() != null) {
					try {
						connection = sshProviderService.openEnvironmentConnection(modelResultMetadata.getExecutionEnvironment().getHostAddress(),
								modelResultMetadata.getExecutionEnvironment().getUsername(), 
								modelResultMetadata.getExecutionEnvironment().getPassword());
						
						JobStatus jobStatus = clusterProviderService.checkJobStatus(connection, modelResultMetadata.getModelExecutor().getJobName());
	
						if (JobStatus.CANCELLED.equals(jobStatus)) {
							modelResultMetadata.getModelExecutor().setExecutionStatus(ExecutionStatus.IDLE);
							modelResultMetadata.setExecutionFinishDate(Calendar.getInstance());
							modelResultMetadata.setExecutionStatus(ExecutionStatus.ABORTED);
							modelResultMetadata.setExecutorExecutionStatus(ExecutionStatus.ABORTED);
							modelResultMetadata.appendSystemLog(String.format("Finished execution with job status [%s]", jobStatus.name()));
							
						} else if (JobStatus.COMPLETED.equals(jobStatus)) {
							modelResultMetadata.getModelExecutor().setExecutionStatus(ExecutionStatus.IDLE);
							modelResultMetadata.setExecutorExecutionStatus(ExecutionStatus.FINISHED);
							modelResultMetadata = modelResultMetadataService.updateExecutionOutput(modelResultMetadata, 
									String.format("Finished execution with job status [%s]", jobStatus.name()));
							
							try {
								String strategyBeanName = String.format("%sInvokerStrategy", 
										modelResultMetadata.getComputationalModel().getType().getTypeName());
								ModelInvoker modelInvokerStrategy = beanFactory.getBean(strategyBeanName, ModelInvoker.class);
								modelResultMetadata = modelInvokerStrategy.handleExtractorExecution(
										connection, modelResultMetadata.getComputationalModel(), modelResultMetadata);
								
								modelResultMetadata.setExecutionStatus(ExecutionStatus.FINISHED);
								
							} catch (Exception e) {
								log.error("Error while extracting metadata of Cluster");
								modelResultMetadata.setExecutionStatus(ExecutionStatus.FAILURE);
							}
							
							modelResultMetadata.setExecutionFinishDate(Calendar.getInstance());
							
						} else if (JobStatus.FAILED.equals(jobStatus) || JobStatus.NODE_FAIL.equals(jobStatus) ||
								JobStatus.TIMEOUT.equals(jobStatus)) {
							
							modelResultMetadata.getModelExecutor().setExecutionStatus(ExecutionStatus.IDLE);
							modelResultMetadata.setExecutionFinishDate(Calendar.getInstance());
							modelResultMetadata.setExecutionStatus(ExecutionStatus.FAILURE);
							modelResultMetadata.setExecutorExecutionStatus(ExecutionStatus.FAILURE);
							modelResultMetadata.appendSystemLog(String.format("Finished execution with job status [%s]", jobStatus.name()));
						}
						
					} catch (Exception e) {
						log.error("Error while checking status of job [{}] of ComputationalModel of slug [{}]", 
								modelResultMetadata.getModelExecutor().getJobName(), modelResultMetadata.getComputationalModel().getSlug(), e);
					} finally {
						if (connection != null) {
							connection.close();
						}
						modelResultMetadata.setModelExecutor(modelExecutorService.update(modelResultMetadata.getModelExecutor()));
						modelResultMetadata = modelResultMetadataService.update(modelResultMetadata);
					}
				}
			}
			
			page++;
			pageRequest = new PageRequest(page, Constants.DEFAULT_LIMIT);
			modelResultMetadatas = modelResultMetadataService
					.findByExecutionEnvironmentTypeAndExecutionStatus(
							EnvironmentType.CLUSTER, ExecutionStatus.RUNNING, pageRequest);
		}
    	log.info("Finished update process of job executions in Cluster");
	}

}