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
import com.uff.phenomanager.domain.ExecutionStatus;
import com.uff.phenomanager.domain.Experiment;
import com.uff.phenomanager.domain.ExtractorMetadata;
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
			
		} else if (modelExecutionMessageDto.getModelMetadataExtractorSlug() != null && 
				!"".equals(modelExecutionMessageDto.getModelMetadataExtractorSlug())) {
			handleRunExtractor(modelExecutionMessageDto);
		
		} else {
			throw new BadRequestApiException(Constants.MSG_ERROR.COMPUTATIONAL_MODEL_INVALID_TARGET_ERROR);
		}
	}
	
	private void handleRunExecutor(ModelExecutionMessageDto modelExecutionMessageDto)
			throws BadRequestApiException, ApiException {
		
		ModelExecutor modelExecutor = getExecutor(modelExecutionMessageDto);
		
		handleExecutorEnvironment(modelExecutionMessageDto, modelExecutor);
		validateExecutorStatus(modelExecutionMessageDto, modelExecutor);
		
		if (ExecutionCommand.START.equals(modelExecutionMessageDto.getExecutionCommand())) {
			modelExecutor.setExecutionStatus(ExecutionStatus.SCHEDULED);
			modelExecutorService.update(modelExecutor);
			modelExecutionSender.sendMessage(modelExecutionMessageDto);
		} else {
			modelKillerSender.sendMessage(modelExecutionMessageDto);
		}
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

	private void validateExecutorStatus(ModelExecutionMessageDto modelExecutionMessageDto, ModelExecutor modelExecutor)
			throws BadRequestApiException {
		if (ExecutionCommand.START.equals(modelExecutionMessageDto.getExecutionCommand())) {
			if (ExecutionStatus.RUNNING.equals(modelExecutor.getExecutionStatus()) || 
					ExecutionStatus.SCHEDULED.equals(modelExecutor.getExecutionStatus())) {
				throw new BadRequestApiException(Constants.MSG_ERROR.EXECUTOR_ALREADY_RUNNING_ERROR);
			}
		}
		
		if (ExecutionCommand.STOP.equals(modelExecutionMessageDto.getExecutionCommand())) {
			if (!ExecutionStatus.RUNNING.equals(modelExecutor.getExecutionStatus())) {
				throw new BadRequestApiException(Constants.MSG_ERROR.EXECUTOR_NOT_RUNNING_ERROR);
			}
		}
	}

	private void handleExecutorEnvironment(ModelExecutionMessageDto modelExecutionMessageDto,
			ModelExecutor modelExecutor) throws BadRequestApiException {
		
		if (modelExecutionMessageDto.getExecutionEnvironmentSlug() != null && 
				!"".equals(modelExecutionMessageDto.getExecutionEnvironmentSlug())) {
			try {
				executionEnvironmentService.findBySlug(modelExecutionMessageDto.getExecutionEnvironmentSlug());
			} catch (NotFoundApiException e) {
				throw new BadRequestApiException(Constants.MSG_ERROR.ENVIRONMENT_NOT_FOUND_ERROR);
			}
			
		} else {
			if (ExecutionCommand.START.equals(modelExecutionMessageDto.getExecutionCommand())) {
				throw new BadRequestApiException(Constants.MSG_ERROR.ENVIRONMENT_NOT_FOUND_ERROR);
			
			} else if (ExecutionCommand.STOP.equals(modelExecutionMessageDto.getExecutionCommand())) {
				ModelResultMetadata modelResultMetadata = modelResultMetadataService
						.findByModelExecutorAndExecutionStatus(modelExecutor, ExecutionStatus.RUNNING);
				
				if (modelResultMetadata == null) {
					throw new BadRequestApiException(Constants.MSG_ERROR.EXECUTOR_NOT_RUNNING_ERROR);
				} else {
					modelExecutionMessageDto.setExecutionEnvironmentSlug(modelResultMetadata.getExecutionEnvironment().getSlug());
				}
				
			} else {
				throw new BadRequestApiException(Constants.MSG_ERROR.COMPUTATIONAL_MODEL_INVALID_TARGET_ERROR);
			}
		}
	}
	
	private void handleRunExtractor(ModelExecutionMessageDto modelExecutionMessageDto) throws ApiException {
		if (modelExecutionMessageDto.getExecutionEnvironmentSlug() == null || 
				"".equals(modelExecutionMessageDto.getExecutionEnvironmentSlug())) {
			throw new BadRequestApiException(Constants.MSG_ERROR.ENVIRONMENT_NOT_FOUND_ERROR);
		}
		
		try {
			executionEnvironmentService.findBySlug(modelExecutionMessageDto.getExecutionEnvironmentSlug());
		} catch (NotFoundApiException e) {
			throw new BadRequestApiException(Constants.MSG_ERROR.ENVIRONMENT_NOT_FOUND_ERROR);
		}
		
		ModelMetadataExtractor modelMetadataExtractor = getExtractor(modelExecutionMessageDto);
		handleExtractorEnvironment(modelExecutionMessageDto, modelMetadataExtractor);
		validateExtractorStatus(modelExecutionMessageDto, modelMetadataExtractor);
		
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
	
	private void validateExtractorStatus(ModelExecutionMessageDto modelExecutionMessageDto, ModelMetadataExtractor modelMetadataExtractor)
			throws BadRequestApiException {
		if (ExecutionCommand.START.equals(modelExecutionMessageDto.getExecutionCommand())) {
			if (ExecutionStatus.RUNNING.equals(modelMetadataExtractor.getExecutionStatus()) || 
					ExecutionStatus.SCHEDULED.equals(modelMetadataExtractor.getExecutionStatus())) {
				throw new BadRequestApiException(Constants.MSG_ERROR.EXTRACTOR_ALREADY_RUNNING_ERROR);
			}
		}
		
		if (ExecutionCommand.STOP.equals(modelExecutionMessageDto.getExecutionCommand())) {
			if (!ExecutionStatus.RUNNING.equals(modelMetadataExtractor.getExecutionStatus())) {
				throw new BadRequestApiException(Constants.MSG_ERROR.EXTRACTOR_NOT_RUNNING_ERROR);
			}
		}
	}
	
	private void handleExtractorEnvironment(ModelExecutionMessageDto modelExecutionMessageDto,
			ModelMetadataExtractor modelMetadataExtractor) throws BadRequestApiException {
		if (modelExecutionMessageDto.getExecutionEnvironmentSlug() != null && 
				!"".equals(modelExecutionMessageDto.getExecutionEnvironmentSlug())) {
			try {
				executionEnvironmentService.findBySlug(modelExecutionMessageDto.getExecutionEnvironmentSlug());
			} catch (NotFoundApiException e) {
				throw new BadRequestApiException(Constants.MSG_ERROR.ENVIRONMENT_NOT_FOUND_ERROR);
			}
			
		} else {
			if (ExecutionCommand.START.equals(modelExecutionMessageDto.getExecutionCommand())) {
				throw new BadRequestApiException(Constants.MSG_ERROR.ENVIRONMENT_NOT_FOUND_ERROR);
			
			} else if (ExecutionCommand.STOP.equals(modelExecutionMessageDto.getExecutionCommand())) {
				ExtractorMetadata extractorMetadata = extractorMetadataService
						.findByModelMetadataExtractorAndExecutionStatus(modelMetadataExtractor, ExecutionStatus.RUNNING);
				
				if (extractorMetadata == null) {
					throw new BadRequestApiException(Constants.MSG_ERROR.EXTRACTOR_NOT_RUNNING_ERROR);
				} else {
					modelExecutionMessageDto.setExecutionEnvironmentSlug(
							extractorMetadata.getModelResultMetadata().getExecutionEnvironment().getSlug());
				}
				
			} else {
				throw new BadRequestApiException(Constants.MSG_ERROR.COMPUTATIONAL_MODEL_INVALID_TARGET_ERROR);
			}
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