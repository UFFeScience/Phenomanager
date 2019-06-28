package com.uff.phenomanager.service;

import java.util.Calendar;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.uff.phenomanager.Constants;
import com.uff.phenomanager.amqp.ModelExecutionSender;
import com.uff.phenomanager.config.security.TokenAuthenticationService;
import com.uff.phenomanager.domain.ComputationalModel;
import com.uff.phenomanager.domain.ExecutionCommand;
import com.uff.phenomanager.domain.ExecutionStatus;
import com.uff.phenomanager.domain.Experiment;
import com.uff.phenomanager.domain.ModelExecutor;
import com.uff.phenomanager.domain.ModelMetadataExtractor;
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
	private ModelExecutorService modelExecutorService;
	
	@Autowired
	private ModelMetadataExtractorService modelMetadataExtractorService;
	
	@Autowired
	private ModelResultMetadataService modelResultMetadataService;
	
	@Autowired
	private InstanceParamService instanceParamService;
	
	@Autowired
	private ModelExecutionSender modelExecutionSender;
	
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
		
		if (modelExecutionMessageDto.getModelMetadataExtractorSlug() != null && 
				!"".equals(modelExecutionMessageDto.getModelMetadataExtractorSlug())) {
			
			ModelMetadataExtractor modelMetadataExtractor = modelMetadataExtractorService
					.findByComputationalModelAndActive(computationalModel, Boolean.TRUE);
			
			if (modelMetadataExtractor == null) {
				throw new BadRequestApiException(Constants.MSG_ERROR.EXTRACTOR_NOT_FOUND_ERROR);
			}
			
			if (ExecutionStatus.RUNNING.equals(modelMetadataExtractor.getExecutionStatus()) || 
					ExecutionStatus.SCHEDULED.equals(modelMetadataExtractor.getExecutionStatus())) {
				throw new BadRequestApiException(Constants.MSG_ERROR.COMPUTATIONAL_MODEL_ALREADY_RUNNING_ERROR);
			}
			
			modelExecutionSender.sendMessage(modelExecutionMessageDto);
			return;
		}
		
		ModelExecutor modelExecutor = null;
		
		if (modelExecutionMessageDto.getComputationalModelSlug() != null && 
				!"".equals(modelExecutionMessageDto.getComputationalModelSlug())) {
			
			modelExecutor = modelExecutorService.findByComputationalModelAndActive(computationalModel, Boolean.TRUE);
		
		} else {
			modelExecutor = modelExecutorService.findBySlug(modelExecutionMessageDto.getComputationalModelSlug());
		}
		
		if (modelExecutor == null) {
			throw new BadRequestApiException(Constants.MSG_ERROR.EXECUTOR_NOT_FOUND_ERROR);
		}
		
		if (ExecutionCommand.START.equals(modelExecutionMessageDto.getExecutionCommand())) {
			if (ExecutionStatus.RUNNING.equals(modelExecutor.getExecutionStatus()) || 
					ExecutionStatus.SCHEDULED.equals(modelExecutor.getExecutionStatus())) {
				throw new BadRequestApiException(Constants.MSG_ERROR.COMPUTATIONAL_MODEL_ALREADY_RUNNING_ERROR);
			}
			
			List<ModelExecutor> allRunningExecutors = modelExecutorService.findAllByComputationalModelAndExecutionStatus(
					computationalModel, ExecutionStatus.RUNNING);
			
			if (allRunningExecutors != null && !allRunningExecutors.isEmpty()) {
				throw new BadRequestApiException(Constants.MSG_ERROR.ANY_COMPUTATIONAL_MODEL_ALREADY_RUNNING_ERROR);
			}
		}
		
		if (ExecutionCommand.STOP.equals(modelExecutionMessageDto.getExecutionCommand())) {
			if (!ExecutionStatus.RUNNING.equals(modelExecutor.getExecutionStatus())) {
				throw new BadRequestApiException(Constants.MSG_ERROR.COMPUTATIONAL_MODEL_NOT_RUNNING_ERROR);
			}
		}
		
		modelExecutor.setExecutionStatus(ExecutionStatus.SCHEDULED);
		modelExecutorService.update(modelExecutor);
		
		modelExecutionSender.sendMessage(modelExecutionMessageDto);
	}
	
	@Override
	public Integer delete(String slug) throws NotFoundApiException {
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