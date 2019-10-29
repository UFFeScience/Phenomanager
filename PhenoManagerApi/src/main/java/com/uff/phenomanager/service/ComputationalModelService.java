package com.uff.phenomanager.service;

import java.util.Calendar;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.uff.phenomanager.Constants;
import com.uff.phenomanager.amqp.ModelExecutionSender;
import com.uff.phenomanager.amqp.ModelKillerSender;
import com.uff.phenomanager.config.security.TokenAuthenticationService;
import com.uff.phenomanager.domain.ComputationalModel;
import com.uff.phenomanager.domain.ExecutionCommand;
import com.uff.phenomanager.domain.ExecutionEnvironment;
import com.uff.phenomanager.domain.ExecutionStatus;
import com.uff.phenomanager.domain.Experiment;
import com.uff.phenomanager.domain.ModelExecutor;
import com.uff.phenomanager.domain.ModelMetadataExtractor;
import com.uff.phenomanager.domain.ModelResultMetadata;
import com.uff.phenomanager.domain.dto.amqp.ModelExecutionMessageDto;
import com.uff.phenomanager.exception.ApiException;
import com.uff.phenomanager.exception.BadRequestApiException;
import com.uff.phenomanager.exception.NotFoundApiException;
import com.uff.phenomanager.repository.ComputationalModelRepository;
import com.uff.phenomanager.service.core.ApiPermissionRestService;
import com.uff.phenomanager.util.KeyUtils;
import com.uff.phenomanager.util.TokenUtils;

@Service
public class ComputationalModelService extends ApiPermissionRestService<ComputationalModel, ComputationalModelRepository> {
	
	@Autowired
	private ComputationalModelRepository computationalModelRepository;
	
	@Autowired
	private ExperimentService experimentService;
	
	@Autowired
	private ExecutionEnvironmentService executionEnvironmentService;
	
	@Autowired
	private ModelExecutorService modelExecutorService;
	
	@Autowired
	private ModelMetadataExtractorService modelMetadataExtractorService;
	
	@Autowired
	private ModelResultMetadataService modelResultMetadataService;
	
	@Autowired
	private ExtractorMetadataService extractorMetadataService;
	
	@Autowired
	private InstanceParamService instanceParamService;
	
	@Autowired
	private ModelExecutionSender modelExecutionSender;
	
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

	public void run(String slug, String authorization, ModelExecutionMessageDto modelExecutionMessageDto) throws ApiException {
		String token = TokenUtils.getTokenFromAuthorizationHeader(authorization);
		String userSlug = tokenAuthenticationService.getTokenClaim(token, Constants.JWT_AUTH.CLAIM_USER_SLUG);
		
		ComputationalModel computationalModel = findBySlug(slug);
		
		modelExecutionMessageDto.setUserSlug(userSlug);
		modelExecutionMessageDto.setExecutionDate(Calendar.getInstance());
		modelExecutionMessageDto.setComputationalModelVersion(computationalModel.getCurrentVersion());
		
		if (modelExecutionMessageDto.getModelExecutorSlug() != null && 
				!"".equals(modelExecutionMessageDto.getModelExecutorSlug())) {
			handleRunExecutor(modelExecutionMessageDto);
			
		} else if (modelExecutionMessageDto.getModelResultMetadataSlug() != null && 
				!"".equals(modelExecutionMessageDto.getModelResultMetadataSlug())) {
			handleRunModelResultMetadata(modelExecutionMessageDto);
		
		} else if (modelExecutionMessageDto.getModelMetadataExtractorSlug() != null && 
				!"".equals(modelExecutionMessageDto.getModelMetadataExtractorSlug())) {
			handleRunExtractor(modelExecutionMessageDto);
		
		} else {
			throw new BadRequestApiException(Constants.MSG_ERROR.COMPUTATIONAL_MODEL_INVALID_TARGET_ERROR);
		}
	}
	
	private void handleRunModelResultMetadata(ModelExecutionMessageDto modelExecutionMessageDto) throws ApiException {
		ModelResultMetadata modelResultMetadata = getModelResultMetadata(modelExecutionMessageDto);
		modelExecutionMessageDto.setExecutionEnvironmentSlug(modelResultMetadata.getExecutionEnvironment().getSlug());
		validateModelResultMetadataStatus(modelExecutionMessageDto, modelResultMetadata);
		
		modelResultMetadata.setHasAbortRequested(Boolean.TRUE);
		modelResultMetadataService.update(modelResultMetadata);
		
		modelKillerSender.sendMessage(modelExecutionMessageDto);
	}
	
	private ModelResultMetadata getModelResultMetadata(ModelExecutionMessageDto modelExecutionMessageDto) throws BadRequestApiException {
		ModelResultMetadata modelResultMetadata = null;
		
		try {
			modelResultMetadata = modelResultMetadataService.findBySlug(modelExecutionMessageDto.getModelResultMetadataSlug());
		} catch (NotFoundApiException e) {
			throw new BadRequestApiException(Constants.MSG_ERROR.METADATA_RESULT_NOT_FOUND_ERROR);
		}
		
		return modelResultMetadata;
	}
	
	private void validateModelResultMetadataStatus(ModelExecutionMessageDto modelExecutionMessageDto, ModelResultMetadata modelResultMetadata)
			throws BadRequestApiException {
		if (ExecutionCommand.STOP.equals(modelExecutionMessageDto.getExecutionCommand())) {
			if (!ExecutionStatus.RUNNING.equals(modelResultMetadata.getExecutionStatus()) && 
					!ExecutionStatus.SCHEDULED.equals(modelResultMetadata.getExecutionStatus())) {
				throw new BadRequestApiException(Constants.MSG_ERROR.EXECUTION_NOT_RUNNING_ERROR);
			}

		} else {
			throw new BadRequestApiException(Constants.MSG_ERROR.COMPUTATIONAL_MODEL_INVALID_TARGET_ERROR);
		}
	}
	
	private void handleRunExecutor(ModelExecutionMessageDto modelExecutionMessageDto) throws ApiException {
		ModelExecutor modelExecutor = getExecutor(modelExecutionMessageDto);
		ExecutionEnvironment executionEnvironment = handleExecutionEnvironment(modelExecutionMessageDto);
		validateExecutorStatus(modelExecutionMessageDto, modelExecutor, executionEnvironment);
		
		modelExecutionSender.sendMessage(modelExecutionMessageDto);
	}

	private ModelExecutor getExecutor(ModelExecutionMessageDto modelExecutionMessageDto) throws BadRequestApiException {
		ModelExecutor modelExecutor = null;
		
		try {
			modelExecutor = modelExecutorService.findBySlug(modelExecutionMessageDto.getModelExecutorSlug());
		} catch (NotFoundApiException e) {
			throw new BadRequestApiException(Constants.MSG_ERROR.EXECUTOR_NOT_FOUND_ERROR);
		}
		
		return modelExecutor;
	}

	private void validateExecutorStatus(ModelExecutionMessageDto modelExecutionMessageDto, ModelExecutor modelExecutor, 
			ExecutionEnvironment executionEnvironment) throws BadRequestApiException {
		
		if (ExecutionCommand.START.equals(modelExecutionMessageDto.getExecutionCommand())) {
			Long totalRunning = modelResultMetadataService.
					countByModelExecutorAndExecutionEnvironmentAndExecutionStatus(
							modelExecutor, executionEnvironment, ExecutionStatus.RUNNING);
			
			if (totalRunning > 0) {
				throw new BadRequestApiException(Constants.MSG_ERROR.EXECUTOR_ALREADY_RUNNING_ERROR);
			}

		} else {
			throw new BadRequestApiException(Constants.MSG_ERROR.COMPUTATIONAL_MODEL_INVALID_TARGET_ERROR);
		}
	}

	private ExecutionEnvironment handleExecutionEnvironment(ModelExecutionMessageDto modelExecutionMessageDto) throws BadRequestApiException {
		if (modelExecutionMessageDto.getExecutionEnvironmentSlug() != null && 
				!"".equals(modelExecutionMessageDto.getExecutionEnvironmentSlug())) {
			try {
				return executionEnvironmentService.findBySlug(modelExecutionMessageDto.getExecutionEnvironmentSlug());
				
			} catch (NotFoundApiException e) {
				throw new BadRequestApiException(Constants.MSG_ERROR.ENVIRONMENT_NOT_FOUND_ERROR);
			}
			
		} else {
			throw new BadRequestApiException(Constants.MSG_ERROR.ENVIRONMENT_NOT_FOUND_ERROR);
		}
	}
	
	private void handleRunExtractor(ModelExecutionMessageDto modelExecutionMessageDto) throws ApiException {
		ModelMetadataExtractor modelMetadataExtractor = getExtractor(modelExecutionMessageDto);
		ExecutionEnvironment executionEnvironment = handleExecutionEnvironment(modelExecutionMessageDto);
		validateExtractorStatus(modelExecutionMessageDto, modelMetadataExtractor, executionEnvironment);
		
		modelMetadataExtractorService.update(modelMetadataExtractor);
		modelExecutionSender.sendMessage(modelExecutionMessageDto);
	}
	
	private ModelMetadataExtractor getExtractor(ModelExecutionMessageDto modelExecutionMessageDto) throws BadRequestApiException {
		ModelMetadataExtractor modelMetadataExtractor = null;
		
		try {
			modelMetadataExtractor = modelMetadataExtractorService.findBySlug(modelExecutionMessageDto.getModelMetadataExtractorSlug());
		} catch (NotFoundApiException e) {
			throw new BadRequestApiException(Constants.MSG_ERROR.EXTRACTOR_NOT_FOUND_ERROR);
		}
		
		return modelMetadataExtractor;
	}
	
	private void validateExtractorStatus(ModelExecutionMessageDto modelExecutionMessageDto, ModelMetadataExtractor modelMetadataExtractor,
			ExecutionEnvironment executionEnvironment) throws BadRequestApiException {
		
		if (ExecutionCommand.START.equals(modelExecutionMessageDto.getExecutionCommand())) {

			Long totalRunning = extractorMetadataService.countByModelMetadataExtractorAndExecutionEnvironmentAndExecutionStatus(
					modelMetadataExtractor, executionEnvironment, ExecutionStatus.RUNNING);
					
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
		modelExecutorService.deleteByComputationalModel(computationalModel);
		modelMetadataExtractorService.deleteByComputationalModel(computationalModel);
		modelResultMetadataService.deleteByComputationalModel(computationalModel);
		permissionService.deleteByComputationalModel(computationalModel);
		
		return super.delete(slug);
	}
	
	public List<ComputationalModel> findAllByExperiment(Experiment experiment) {
		return computationalModelRepository.findAllByExperiment(experiment);
	}
	
}