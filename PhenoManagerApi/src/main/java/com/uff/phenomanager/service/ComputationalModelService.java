package com.uff.phenomanager.service;

import java.util.Calendar;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.uff.phenomanager.Constants;
import com.uff.phenomanager.amqp.ModelExecutorSender;
import com.uff.phenomanager.amqp.ModelKillerSender;
import com.uff.phenomanager.domain.ComputationalModel;
import com.uff.phenomanager.domain.Environment;
import com.uff.phenomanager.domain.Execution;
import com.uff.phenomanager.domain.ExecutionCommand;
import com.uff.phenomanager.domain.ExecutionStatus;
import com.uff.phenomanager.domain.Executor;
import com.uff.phenomanager.domain.Experiment;
import com.uff.phenomanager.domain.Extractor;
import com.uff.phenomanager.domain.dto.amqp.ExecutionMessageDto;
import com.uff.phenomanager.exception.ApiException;
import com.uff.phenomanager.exception.BadRequestApiException;
import com.uff.phenomanager.exception.NotFoundApiException;
import com.uff.phenomanager.repository.ComputationalModelRepository;
import com.uff.phenomanager.service.core.ApiPermissionRestService;
import com.uff.phenomanager.service.core.TokenAuthenticationService;
import com.uff.phenomanager.util.KeyUtils;
import com.uff.phenomanager.util.TokenUtils;

@Service
public class ComputationalModelService extends ApiPermissionRestService<ComputationalModel, ComputationalModelRepository> {
	
	@Autowired
	private ComputationalModelRepository computationalModelRepository;
	
	@Autowired
	private ExperimentService experimentService;
	
	@Autowired
	private EnvironmentService environmentService;
	
	@Autowired
	private ExecutorService executorService;
	
	@Autowired
	private ExtractorService extractorService;
	
	@Autowired
	private ExecutionService executionService;
	
	@Autowired
	private ExtractorExecutionService extractorExecutionService;
	
	@Autowired
	private InstanceParamService instanceParamService;
	
	@Autowired
	private ModelExecutorSender modelExecutionSender;
	
	@Autowired
	private ModelKillerSender modelKillerSender;
	
	@Autowired
	private TokenAuthenticationService tokenAuthenticationService;
	
	@Override
	protected ComputationalModelRepository getRepository() {
		return computationalModelRepository;
	}
	
	@Override
	protected Class<ComputationalModel> getEntityClass() {
		return ComputationalModel.class;
	}
	
	@Override
	protected String getPermissionEntityName() {
		return StringUtils.uncapitalize(getEntityClass().getSimpleName());
	}
	
	@Override
	public ComputationalModel save(ComputationalModel computationalModel, String authorization) throws ApiException {
		Experiment parentEntity = null;
		
		if (computationalModel.getSlug() == null || "".equals(computationalModel.getSlug())) {
			computationalModel.setSlug(KeyUtils.generate());
		}
		
		if (computationalModel.getExperiment() == null || computationalModel.getExperiment().getSlug() == null ||
				"".equals(computationalModel.getExperiment().getSlug())) {
			
			throw new BadRequestApiException(String.format(Constants.MSG_ERROR.PARENT_ENTITY_NULL_ERROR, Experiment.class.getName()));
		}
		
		try {
			parentEntity = experimentService.findBySlug(computationalModel.getExperiment().getSlug());
		} catch (NotFoundApiException e) {
			throw new BadRequestApiException(
					String.format(Constants.MSG_ERROR.PARENT_ENTITY_NOT_FOUND_ERROR, Experiment.class.getName(),
							computationalModel.getExperiment().getSlug()), e);
		}
		
		computationalModel.setExperiment(parentEntity);
		
		return super.save(computationalModel, authorization);
	}
	
	@Override
	public ComputationalModel update(ComputationalModel computationalModel) throws ApiException {
		ComputationalModel computationalModelDatabase = findBySlug(computationalModel.getSlug());
		computationalModel.setId(computationalModelDatabase.getId());
		
		Experiment parentEntity = null;
		
		if (computationalModel.getExperiment() == null || computationalModel.getExperiment().getSlug() == null ||
				"".equals(computationalModel.getExperiment().getSlug())) {
			
			throw new BadRequestApiException(String.format(Constants.MSG_ERROR.PARENT_ENTITY_NULL_ERROR, Experiment.class.getName()));
		}
		
		try {
			parentEntity = experimentService.findBySlug(computationalModel.getExperiment().getSlug());
		} catch (NotFoundApiException e) {
			throw new BadRequestApiException(
					String.format(Constants.MSG_ERROR.PARENT_ENTITY_NOT_FOUND_ERROR, Experiment.class.getName(),
							computationalModel.getExperiment().getSlug()), e);
		}
		
		computationalModel.setExperiment(parentEntity);

		return super.update(computationalModel);
	}

	public void run(String slug, String authorization, ExecutionMessageDto executionMessageDto) throws ApiException {
		String token = TokenUtils.getTokenFromAuthorizationHeader(authorization);
		String userSlug = tokenAuthenticationService.getTokenClaim(token, Constants.JWT_AUTH.CLAIM_USER_SLUG);
		
		ComputationalModel computationalModel = findBySlug(slug);
		
		executionMessageDto.setUserSlug(userSlug);
		executionMessageDto.setExecutionDate(Calendar.getInstance());
		executionMessageDto.setComputationalModelVersion(computationalModel.getCurrentVersion());
		
		if (executionMessageDto.getExecutorSlug() != null && 
				!"".equals(executionMessageDto.getExecutorSlug())) {
			handleRunExecutor(executionMessageDto);
			
		} else if (executionMessageDto.getExecutionSlug() != null && 
				!"".equals(executionMessageDto.getExecutionSlug())) {
			handleRunExecution(executionMessageDto);
		
		} else if (executionMessageDto.getExtractorSlug() != null && 
				!"".equals(executionMessageDto.getExtractorSlug())) {
			handleRunExtractor(executionMessageDto);
		
		} else {
			throw new BadRequestApiException(Constants.MSG_ERROR.COMPUTATIONAL_MODEL_INVALID_TARGET_ERROR);
		}
	}
	
	private void handleRunExecution(ExecutionMessageDto executionMessageDto) throws ApiException {
		Execution execution = getExecution(executionMessageDto);
		executionMessageDto.setEnvironmentSlug(execution.getEnvironment().getSlug());
		validateExecutionStatus(executionMessageDto, execution);
		
		execution.setHasAbortRequested(Boolean.TRUE);
		executionService.update(execution);
		
		modelKillerSender.sendMessage(executionMessageDto);
	}
	
	private Execution getExecution(ExecutionMessageDto executionMessageDto) throws BadRequestApiException {
		Execution execution = null;
		
		try {
			execution = executionService.findBySlug(executionMessageDto.getExecutionSlug());
		} catch (NotFoundApiException e) {
			throw new BadRequestApiException(Constants.MSG_ERROR.METADATA_RESULT_NOT_FOUND_ERROR);
		}
		
		return execution;
	}
	
	private void validateExecutionStatus(ExecutionMessageDto executionMessageDto, Execution execution)
			throws BadRequestApiException {
		if (ExecutionCommand.STOP.equals(executionMessageDto.getExecutionCommand())) {
			if (!ExecutionStatus.RUNNING.equals(execution.getStatus()) && 
					!ExecutionStatus.SCHEDULED.equals(execution.getStatus())) {
				throw new BadRequestApiException(Constants.MSG_ERROR.EXECUTION_NOT_RUNNING_ERROR);
			}

		} else {
			throw new BadRequestApiException(Constants.MSG_ERROR.COMPUTATIONAL_MODEL_INVALID_TARGET_ERROR);
		}
	}
	
	private void handleRunExecutor(ExecutionMessageDto executionMessageDto) throws ApiException {
		Executor executor = getExecutor(executionMessageDto);
		Environment environment = handleEnvironment(executionMessageDto);
		validateExecutorStatus(executionMessageDto, executor, environment);
		
		modelExecutionSender.sendMessage(executionMessageDto);
	}

	private Executor getExecutor(ExecutionMessageDto executionMessageDto) throws BadRequestApiException {
		Executor executor = null;
		
		try {
			executor = executorService.findBySlug(executionMessageDto.getExecutorSlug());
		} catch (NotFoundApiException e) {
			throw new BadRequestApiException(Constants.MSG_ERROR.EXECUTOR_NOT_FOUND_ERROR);
		}
		
		return executor;
	}

	private void validateExecutorStatus(ExecutionMessageDto executionMessageDto, Executor executor, Environment environment) throws BadRequestApiException {
		
		if (ExecutionCommand.START.equals(executionMessageDto.getExecutionCommand())) {
			Long totalRunning = executionService.countByExecutorAndEnvironmentAndStatus(executor, environment, ExecutionStatus.RUNNING);
			
			if (totalRunning > 0) {
				throw new BadRequestApiException(Constants.MSG_ERROR.EXECUTOR_ALREADY_RUNNING_ERROR);
			}

		} else {
			throw new BadRequestApiException(Constants.MSG_ERROR.COMPUTATIONAL_MODEL_INVALID_TARGET_ERROR);
		}
	}

	private Environment handleEnvironment(ExecutionMessageDto executionMessageDto) throws BadRequestApiException {
		if (executionMessageDto.getEnvironmentSlug() != null && 
				!"".equals(executionMessageDto.getEnvironmentSlug())) {
			try {
				return environmentService.findBySlug(executionMessageDto.getEnvironmentSlug());
				
			} catch (NotFoundApiException e) {
				throw new BadRequestApiException(Constants.MSG_ERROR.ENVIRONMENT_NOT_FOUND_ERROR);
			}
			
		} else {
			throw new BadRequestApiException(Constants.MSG_ERROR.ENVIRONMENT_NOT_FOUND_ERROR);
		}
	}
	
	private void handleRunExtractor(ExecutionMessageDto executionMessageDto) throws ApiException {
		Extractor extractor = getExtractor(executionMessageDto);
		Environment environment = handleEnvironment(executionMessageDto);
		validateExtractorStatus(executionMessageDto, extractor, environment);
		
		extractorService.update(extractor);
		modelExecutionSender.sendMessage(executionMessageDto);
	}
	
	private Extractor getExtractor(ExecutionMessageDto executionMessageDto) throws BadRequestApiException {
		Extractor extractor = null;
		
		try {
			extractor = extractorService.findBySlug(executionMessageDto.getExtractorSlug());
		} catch (NotFoundApiException e) {
			throw new BadRequestApiException(Constants.MSG_ERROR.EXTRACTOR_NOT_FOUND_ERROR);
		}
		
		return extractor;
	}
	
	private void validateExtractorStatus(ExecutionMessageDto executionMessageDto, Extractor extractor, Environment environment) throws BadRequestApiException {
		
		if (ExecutionCommand.START.equals(executionMessageDto.getExecutionCommand())) {

			Long totalRunning = extractorExecutionService.countByExtractorAndExecutionEnvironmentAndExecutionStatus(
					extractor, environment, ExecutionStatus.RUNNING);
					
			if (totalRunning > 0) {
				throw new BadRequestApiException(Constants.MSG_ERROR.EXTRACTOR_ALREADY_RUNNING_ERROR);
			}
		
		} else {
			throw new BadRequestApiException(Constants.MSG_ERROR.COMPUTATIONAL_MODEL_INVALID_TARGET_ERROR);
		}
	}
	
	@Override
	public Integer delete(String slug) throws ApiException {
		ComputationalModel computationalModel = findBySlug(slug);
		
		instanceParamService.deleteByComputationalModel(computationalModel);
		executorService.deleteByComputationalModel(computationalModel);
		extractorService.deleteByComputationalModel(computationalModel);
		executionService.deleteByComputationalModel(computationalModel);
		permissionService.deleteByComputationalModel(computationalModel);
		
		return super.delete(slug);
	}
	
	public List<ComputationalModel> findAllByExperiment(Experiment experiment) {
		return computationalModelRepository.findAllByExperiment(experiment);
	}
	
}