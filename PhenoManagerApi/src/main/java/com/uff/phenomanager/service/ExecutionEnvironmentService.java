package com.uff.phenomanager.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.uff.phenomanager.Constants;
import com.uff.phenomanager.domain.ComputationalModel;
import com.uff.phenomanager.domain.ExecutionEnvironment;
import com.uff.phenomanager.domain.ExecutionStatus;
import com.uff.phenomanager.domain.Experiment;
import com.uff.phenomanager.exception.ApiException;
import com.uff.phenomanager.exception.BadRequestApiException;
import com.uff.phenomanager.exception.NotFoundApiException;
import com.uff.phenomanager.repository.ExecutionEnvironmentRepository;
import com.uff.phenomanager.service.core.ApiPermissionRestService;
import com.uff.phenomanager.util.IpAddressValidator;
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
	private ModelResultMetadataService modelResultMetadataService;
	
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
		
		executionEnvironment.setComputationalModel(parentComputationalModel);
		ExecutionEnvironment executionEnvironmentSaved = super.save(executionEnvironment);
		
		if (!new IpAddressValidator().validateWorkspaceAddress(executionEnvironment.getHostAddress())) {
			throw new BadRequestApiException(
					String.format(Constants.MSG_ERROR.INVALID_ENVIRONMENT_WORKSPACE_ADDRESS, executionEnvironment.getHostAddress()));
		}
		
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
		
		return executionEnvironmentUpdated;
	}
	
	@Override
	public Integer delete(String slug) throws ApiException {
		ExecutionEnvironment executionEnvironment = findBySlug(slug);

		Long relatedExecutionCount = modelResultMetadataService.countByExecutionEnvironmentAndExecutionStatus(
				executionEnvironment, ExecutionStatus.RUNNING);
		
		if (relatedExecutionCount > 0) {
			throw new BadRequestApiException(Constants.MSG_ERROR.ENVIRONMENT_CAN_NOT_BE_DELETED_ERROR);
		}
		
		return super.delete(slug);
	}
	
}