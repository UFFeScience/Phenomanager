package com.uff.phenomanager.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.uff.phenomanager.Constants;
import com.uff.phenomanager.domain.ComputationalModel;
import com.uff.phenomanager.domain.Environment;
import com.uff.phenomanager.domain.ExecutionStatus;
import com.uff.phenomanager.domain.Experiment;
import com.uff.phenomanager.exception.ApiException;
import com.uff.phenomanager.exception.BadRequestApiException;
import com.uff.phenomanager.exception.NotFoundApiException;
import com.uff.phenomanager.repository.EnvironmentRepository;
import com.uff.phenomanager.service.core.ApiPermissionRestService;
import com.uff.phenomanager.util.IpAddressValidator;
import com.uff.phenomanager.util.KeyUtils;

@Service
public class EnvironmentService extends ApiPermissionRestService<Environment, EnvironmentRepository> {
	
	@Autowired
	private EnvironmentRepository environmentRepository;
	
	@Autowired
	private VirtualMachineService virtualMachineService;
	
	@Autowired
	private ComputationalModelService computationalModelService;
	
	@Autowired
	private ExecutionService executionService;
	
	@Override
	protected EnvironmentRepository getRepository() {
		return environmentRepository;
	}
	
	@Override
	protected Class<Environment> getEntityClass() {
		return Environment.class;
	}
	
	@Override
	protected String getPermissionEntityName() {
		return StringUtils.uncapitalize(ComputationalModel.class.getSimpleName());
	}
	
	public Environment save(Environment environment, String computationalModelSlug) throws ApiException {
		ComputationalModel parentComputationalModel = null;
		
		if (environment.getSlug() == null || "".equals(environment.getSlug())) {
			environment.setSlug(KeyUtils.generate());
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
		
		environment.setComputationalModel(parentComputationalModel);
		Environment environmentSaved = super.save(environment);
		
		if (!new IpAddressValidator().validateWorkspaceAddress(environment.getHostAddress())) {
			throw new BadRequestApiException(
					String.format(Constants.MSG_ERROR.INVALID_ENVIRONMENT_WORKSPACE_ADDRESS, environment.getHostAddress()));
		}
		
		environmentSaved.setVirtualMachines(virtualMachineService.save(environment.getVirtualMachines(), environmentSaved));
		
		return environmentSaved;
   }
	
	public Environment update(Environment environment, String computationalModelSlug) throws ApiException {
		Environment environmentDatabase = findBySlug(environment.getSlug());
		environment.setId(environmentDatabase.getId());
		
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
		
		environment.setComputationalModel(parentComputationalModel);
		Environment environmentUpdated = super.update(environment);
		
		environmentUpdated.setVirtualMachines(virtualMachineService.save(environment.getVirtualMachines(), environmentUpdated));
		
		return environmentUpdated;
	}
	
	@Override
	public Integer delete(String slug) throws ApiException {
		Environment environment = findBySlug(slug);
		Long relatedExecutionCount = executionService.countByEnvironmentAndStatus(environment, ExecutionStatus.RUNNING);
		
		if (relatedExecutionCount > 0) {
			throw new BadRequestApiException(Constants.MSG_ERROR.ENVIRONMENT_CAN_NOT_BE_DELETED_ERROR);
		}
		
		return super.delete(slug);
	}
	
}