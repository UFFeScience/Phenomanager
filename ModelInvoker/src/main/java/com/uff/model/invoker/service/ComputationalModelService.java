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
import org.springframework.util.StringUtils;

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
import com.uff.model.invoker.provider.ClusterProvider;
import com.uff.model.invoker.provider.SshProvider;
import com.uff.model.invoker.repository.ComputationalModelRepository;

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
	private ClusterProvider clusterProvider;
	
	@Autowired
	private SshProvider sshProvider;
	
	@Override
	protected ComputationalModelRepository getRepository() {
		return computationalModelRepository;
	}
	
	@Override
	protected Class<ComputationalModel> getEntityClass() {
		return ComputationalModel.class;
	}
	
	public void invokeModelTaskExecutor(ModelExecutionMessageDto modelExecutionMessageDto, Channel channel, Long tag) throws IOException {
		log.info("Starting execution of executor of ComputationalModel of slug [{}] and ModelExecutor of slug [{}]", 
				modelExecutionMessageDto.getComputationalModelSlug(), modelExecutionMessageDto.getModelExecutorSlug());
		ModelExecutor modelExecutor = null;
		
		if (modelExecutionMessageDto.getComputationalModelSlug() != null &&
				!"".equals(modelExecutionMessageDto.getComputationalModelSlug())) {
			
			ComputationalModel computationalModel = computationalModelRepository.findOneBySlug(modelExecutionMessageDto.getComputationalModelSlug());
			
			if (computationalModel == null) {
				log.error("Error while invoking process, ComputationalModel of slug [{}] not found", modelExecutionMessageDto.getComputationalModelSlug());
				channel.basicAck(tag, Boolean.TRUE);
				return;
			}
			
			modelExecutor = modelExecutorService
					.findByComputationalModelAndActive(computationalModel, Boolean.TRUE);
		} else {
			modelExecutor = modelExecutorService.findBySlug(modelExecutionMessageDto.getModelExecutorSlug());
		}
		
		if (modelExecutor == null) {
			log.error("Error while invoking process, ModelExecutor of slug [{}] not found", modelExecutionMessageDto.getModelExecutorSlug());
			channel.basicAck(tag, Boolean.TRUE);
			return;
		}
		
		User user = userService.findBySlug(modelExecutionMessageDto.getUserSlug());
		if (user == null) {
			log.error("Error while invoking process, User of slug [{}] not found", modelExecutionMessageDto.getUserSlug());
			channel.basicAck(tag, Boolean.TRUE);
			return;
		}
		
		try {
			modelExecutor.setUserAgent(user);
			
			if (!canAccessComputationalModel(user, modelExecutor.getComputationalModel())) {
				log.error("User of slug [{}] doesn't have permission to manage actions on ComputationalModel of slug [{}]", 
						user.getSlug(), modelExecutor.getComputationalModel().getSlug());
				
				modelExecutor.setExecutionStatus(ExecutionStatus.FINISHED);
				modelExecutorService.update(modelExecutor);
				channel.basicAck(tag, Boolean.TRUE);
				return;
			}
			
			String strategyBeanName = String.format("%sInvokerStrategy", StringUtils.uncapitalize(
					modelExecutor.getComputationalModel().getType().name()));
			ModelInvoker modelInvokerStrategy = beanFactory.getBean(strategyBeanName, ModelInvoker.class);
			
			if (ExecutionCommand.START.equals(modelExecutionMessageDto.getExecutionCommand())) {
				
				if (modelExecutor.getExecutionStatus().equals(ExecutionStatus.RUNNING)) {
					log.warn("Error while invoking process, ModelExecutor of slug [{}] of ComputationalModel of slug [{}] is already RUNNING", 
							modelExecutor.getSlug(), modelExecutor.getComputationalModel().getSlug());
					channel.basicAck(tag, Boolean.TRUE);
					return;
				}
				modelExecutor.setExecutionStatus(ExecutionStatus.RUNNING);
				modelExecutor = modelExecutorService.update(modelExecutor);
				
				modelInvokerStrategy.startModelExecutor(modelExecutor);
				modelExecutorService.update(modelExecutor);
				
			} else if (ExecutionCommand.STOP.equals(modelExecutionMessageDto.getExecutionCommand())) {
				
				if (modelExecutor.getExecutionStatus().equals(ExecutionStatus.RUNNING)) {
					log.warn("Error while invoking process, ComputationalModel of slug [{}] is not RUNNING, so it can't be stopped", 
							modelExecutor.getComputationalModel().getSlug());
					channel.basicAck(tag, Boolean.TRUE);
					return;
				}
				
				modelExecutor.setExecutionStatus(ExecutionStatus.RUNNING);
				modelExecutor = modelExecutorService.update(modelExecutor);
				
				modelInvokerStrategy.stopModelExecutor(modelExecutor);
				modelExecutorService.update(modelExecutor);
				
			} else {
				log.warn("Unexpected command [{}] for ComputationalModel of slug [{}]", 
						modelExecutionMessageDto.getExecutionCommand(), modelExecutor.getComputationalModel().getSlug());
				
				modelExecutor.setExecutionStatus(ExecutionStatus.FAILURE);
				modelExecutorService.update(modelExecutor);
			}
		}
		catch (Exception e) {
			log.error("Unexpected error while invoking ComputationalModel", e);
			
			modelExecutor.setExecutionStatus(ExecutionStatus.FAILURE);
			modelExecutorService.update(modelExecutor);
			
			channel.basicAck(tag, Boolean.TRUE);
			return;
		} 

		channel.basicAck(tag, Boolean.TRUE);
		log.info("Execution Process of ComputationalModel started with success for ComputationalModel of slug [{}], currentVersion [{}] and ModelExecutor of Slug", 
				modelExecutor.getComputationalModel().getSlug(), modelExecutor.getComputationalModel().getCurrentVersion(), modelExecutor.getSlug());
	}
	
	public void invokeModelTaskExtractor(ModelExecutionMessageDto modelExecutionMessageDto, Channel channel, Long tag) throws IOException {
		log.info("Starting execution of extractor of ComputationalModel of slug [{}] and ModelExecutor of slug [{}]", 
				modelExecutionMessageDto.getComputationalModelSlug(), modelExecutionMessageDto.getModelMetadataExtractorSlug());
	
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
		
		User user = userService.findBySlug(modelExecutionMessageDto.getUserSlug());
		if (user == null) {
			log.error("Error while invoking process, User of slug [{}] not found", modelExecutionMessageDto.getUserSlug());
			channel.basicAck(tag, Boolean.TRUE);
			return;
		}
		
		try {
			modelMetadataExtractor.setUserAgent(user);

			if (!canAccessComputationalModel(user, modelMetadataExtractor.getComputationalModel())) {
				log.error("User of slug [{}] doesn't have permission to manage actions on ComputationalModel of slug [{}]", 
						user.getSlug(), modelMetadataExtractor.getComputationalModel().getSlug());
				
				modelMetadataExtractor.setExecutionStatus(ExecutionStatus.FAILURE);
				modelMetadataExtractorService.update(modelMetadataExtractor);
				channel.basicAck(tag, Boolean.TRUE);
				return;
			}
			
			modelMetadataExtractor.setExecutionStatus(ExecutionStatus.RUNNING);
			modelMetadataExtractor = modelMetadataExtractorService.update(modelMetadataExtractor);
			
			String strategyBeanName = String.format("%sInvokerStrategy", StringUtils.uncapitalize(
					modelMetadataExtractor.getComputationalModel().getType().name()));
			ModelInvoker modelInvokerStrategy = beanFactory.getBean(strategyBeanName, ModelInvoker.class);
			
			modelInvokerStrategy.startModelExtractor(modelMetadataExtractor);
			modelMetadataExtractorService.update(modelMetadataExtractor);
		}
		catch (Exception e) {
			log.error("Unexpected error while invoking ComputationalModel extractor", e);
			
			modelMetadataExtractor.setExecutionStatus(ExecutionStatus.FAILURE);
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
		
		List<ExecutionEnvironment> executionEnvironments = executionEnvironmentService
				.findByType(EnvironmentType.CLUSTER, pageRequest);

		while (executionEnvironments != null && !executionEnvironments.isEmpty()) {
			
			for (ExecutionEnvironment executionEnvironment : executionEnvironments) {
				Connection connection = null;
				
				ComputationalModel computationalModel = executionEnvironment.getComputationalModel();
				
				ModelExecutor modelExecutor = modelExecutorService
						.findByComputationalModelAndExecutionStatus(computationalModel, ExecutionStatus.RUNNING);
				
				ModelResultMetadata modelResultMetadata = modelResultMetadataService.findByModelExecutor(modelExecutor);
				
				if (modelExecutor != null) {
					try {
						connection = sshProvider.openEnvironmentConnection(executionEnvironment.getHostAddress(),
								executionEnvironment.getUsername(), executionEnvironment.getPassword());
						
						JobStatus jobStatus = clusterProvider.checkJobStatus(connection, modelExecutor.getJobName());
	
						if (JobStatus.CANCELLED.equals(jobStatus)) {
							modelExecutor.setExecutionStatus(ExecutionStatus.ABORTED);
							modelResultMetadata.setExecutionFinishDate(Calendar.getInstance());
							modelResultMetadata.setExecutionStatus(ExecutionStatus.ABORTED);
							modelResultMetadata.appendExecutionLog(String.format("Finished execution with job status [%s]", jobStatus.name()));
							
						} else if (JobStatus.COMPLETED.equals(jobStatus)) {
							modelExecutor.setExecutionStatus(ExecutionStatus.FINISHED);
							modelResultMetadata.setExecutionFinishDate(Calendar.getInstance());
							modelResultMetadata.setExecutionStatus(ExecutionStatus.FINISHED);
							modelResultMetadata.appendExecutionLog(String.format("Finished execution with job status [%s]", jobStatus.name()));
							
						} else if (JobStatus.FAILED.equals(jobStatus) || JobStatus.NODE_FAIL.equals(jobStatus) ||
								JobStatus.TIMEOUT.equals(jobStatus)) {
							
							modelExecutor.setExecutionStatus(ExecutionStatus.FAILURE);
							modelResultMetadata.setExecutionFinishDate(Calendar.getInstance());
							modelResultMetadata.setExecutionStatus(ExecutionStatus.FINISHED);
							modelResultMetadata.appendExecutionLog(String.format("Finished execution with job status [%s]", jobStatus.name()));
						}
						
					} catch (Exception e) {
						log.error("Error while checking status of job [{}] of ComputationalModel of slug [{}]", 
								modelExecutor.getJobName(), computationalModel.getSlug());
						
					} finally {
						if (connection != null) {
							connection.close();
						}
						
						modelResultMetadataService.update(modelResultMetadata);
						modelExecutorService.update(modelExecutor);
					}
				}
			}
			
			page++;
			pageRequest = new PageRequest(page, Constants.DEFAULT_LIMIT);
			
			executionEnvironments = executionEnvironmentService
					.findByType(EnvironmentType.CLUSTER, pageRequest);
		}
    	
    	log.info("Finished update process of job executions in Cluster");
	}

}