package com.uff.phenomanager.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.uff.phenomanager.Constants;
import com.uff.phenomanager.domain.ComputationalModel;
import com.uff.phenomanager.domain.ExecutionEnvironment;
import com.uff.phenomanager.domain.ExecutionStatus;
import com.uff.phenomanager.domain.Experiment;
import com.uff.phenomanager.domain.ModelExecutor;
import com.uff.phenomanager.exception.ApiException;
import com.uff.phenomanager.exception.BadRequestApiException;
import com.uff.phenomanager.exception.NotFoundApiException;
import com.uff.phenomanager.repository.ExecutionEnvironmentRepository;
import com.uff.phenomanager.service.core.ApiPermissionRestService;
import com.uff.phenomanager.util.KeyUtils;

@Service
public class ExecutionEnvironmentService extends ApiPermissionRestService<ExecutionEnvironment, ExecutionEnvironmentRepository> {
	
	@Autowired
	private ExecutionEnvironmentRepository executionEnvironmentRepository;
	
	@Autowired
	private VirtualMachineConfigService virtualMachineConfigService;
	
	@Autowired
	private ComputationalModelService computationalModelService;
	
	@Autowired
	private ModelExecutorService modelExecutorService;
	
	@Override
	protected ExecutionEnvironmentRepository getRepository() {
		return executionEnvironmentRepository;
	}
	
	@Override
	protected Class<ExecutionEnvironment> getEntityClass() {
		return ExecutionEnvironment.class;
	}
	
	@Override
	protected String getPermissionEntityName() {
		return StringUtils.uncapitalize(ComputationalModel.class.getSimpleName());
	}
	
	public ExecutionEnvironment save(ExecutionEnvironment executionEnvironment, String computationalModelSlug) throws ApiException {
		ComputationalModel parentComputationalModel = null;
		
		if (executionEnvironment.getSlug() == null || "".equals(executionEnvironment.getSlug())) {
			executionEnvironment.setSlug(KeyUtils.generate());
		}
		
		if (computationalModelSlug == null || "".equals(computationalModelSlug)) {
			throw new BadRequestApiException(String.format(Constants.MSG_ERROR.PARENT_ENTITY_NULL_ERROR, ComputationalModel.class.getName()));
		}
		
		try {
			parentComputationalModel = computationalModelService.findBySlug(computationalModelSlug);
		} catch (NotFoundApiException e) {
			throw new BadRequestApiException(
					String.format(Constants.MSG_ERROR.PARENT_ENTITY_NOT_FOUND_ERROR, ComputationalModel.class.getName(), 
							computationalModelSlug), e);
		}
		
		ExecutionEnvironment currentExecutionEnvironment = findByComputationalModelAndActive(parentComputationalModel, Boolean.TRUE);
		ModelExecutor currentModelExecutorActive = modelExecutorService
				.findByComputationalModelAndActive(parentComputationalModel, Boolean.TRUE);
		
		if (currentModelExecutorActive != null &&
				!currentModelExecutorActive.getExecutionStatus().equals(ExecutionStatus.RUNNING)) {
			
			if (currentExecutionEnvironment != null) {
				currentExecutionEnvironment.setActive(Boolean.FALSE);
				super.update(currentExecutionEnvironment);
			} 
			
		} else if (currentModelExecutorActive != null &&
				currentModelExecutorActive.getExecutionStatus().equals(ExecutionStatus.RUNNING)) {
			executionEnvironment.setActive(Boolean.FALSE);
		}
		
		executionEnvironment.setComputationalModel(parentComputationalModel);
		ExecutionEnvironment executionEnvironmentSaved = super.save(executionEnvironment);
		
		executionEnvironmentSaved.setVirtualMachines(virtualMachineConfigService.save(
				executionEnvironment.getVirtualMachines(), executionEnvironmentSaved));
		
		return executionEnvironmentSaved;
   }
	
	public ExecutionEnvironment update(ExecutionEnvironment executionEnvironment, String computationalModelSlug) throws ApiException {
		ExecutionEnvironment executionEnvironmentDatabase = findBySlug(executionEnvironment.getSlug());
		executionEnvironment.setId(executionEnvironmentDatabase.getId());
		
		ComputationalModel parentComputationalModel = null;
		
		if (computationalModelSlug == null || "".equals(computationalModelSlug)) {
			throw new BadRequestApiException(String.format(Constants.MSG_ERROR.PARENT_ENTITY_NULL_ERROR, ComputationalModel.class.getName()));
		}
		
		try {
			parentComputationalModel = computationalModelService.findBySlug(computationalModelSlug);
		} catch (NotFoundApiException e) {
			throw new BadRequestApiException(
					String.format(Constants.MSG_ERROR.PARENT_ENTITY_NOT_FOUND_ERROR, Experiment.class.getName(), 
							computationalModelSlug), e);
		}
		
		executionEnvironment.setComputationalModel(parentComputationalModel);
		ExecutionEnvironment executionEnvironmentUpdated = super.update(executionEnvironment);
		
		executionEnvironmentUpdated.setVirtualMachines(virtualMachineConfigService.save(
				executionEnvironment.getVirtualMachines(), executionEnvironmentUpdated));
		
		if (executionEnvironmentUpdated.getActive()) {
			ExecutionEnvironment currentExecutionEnvironment = findByComputationalModelAndActive(parentComputationalModel, Boolean.TRUE);
			
			if (!currentExecutionEnvironment.getSlug().equals(executionEnvironmentUpdated.getSlug())) {
		
				ModelExecutor currentModelExecutorActive = modelExecutorService
						.findByComputationalModelAndActive(parentComputationalModel, Boolean.TRUE);
				
				if (currentModelExecutorActive != null &&
						!currentModelExecutorActive.getExecutionStatus().equals(ExecutionStatus.RUNNING)) {
				
					currentExecutionEnvironment.setActive(Boolean.FALSE);
					super.update(currentExecutionEnvironment);
			
				} else if (currentModelExecutorActive != null &&
						currentModelExecutorActive.getExecutionStatus().equals(ExecutionStatus.RUNNING)) {
					executionEnvironment.setActive(Boolean.FALSE);
				}
			}
		}
		
		return executionEnvironmentUpdated;
	}
	
	public ExecutionEnvironment findByComputationalModelAndActive(ComputationalModel computationalModel, Boolean active) {
		return executionEnvironmentRepository.findByComputationalModelAndActive(computationalModel, active);
	}
	
}