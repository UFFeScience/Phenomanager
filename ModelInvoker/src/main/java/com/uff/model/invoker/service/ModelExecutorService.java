package com.uff.model.invoker.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.uff.model.invoker.domain.ComputationalModel;
import com.uff.model.invoker.domain.ExecutionStatus;
import com.uff.model.invoker.domain.ModelExecutor;
import com.uff.model.invoker.repository.ModelExecutorRepository;

@Service
public class ModelExecutorService extends ApiRestService<ModelExecutor, ModelExecutorRepository> {
	
	@Autowired
	private ModelExecutorRepository modelExecutorRepository;
	
	@Override
	protected ModelExecutorRepository getRepository() {
		return modelExecutorRepository;
	}
	
	@Override
	protected Class<ModelExecutor> getEntityClass() {
		return ModelExecutor.class;
	}
	
	public ModelExecutor findByComputationalModelAndActive(ComputationalModel computationalModel, Boolean active) {
		return modelExecutorRepository.findByComputationalModelAndActive(computationalModel, active);
	}
	
	public ModelExecutor findByComputationalModelAndExecutionStatus(ComputationalModel computationalModel, 
			ExecutionStatus executionStatus) {
		return modelExecutorRepository.findByComputationalModelAndExecutionStatus(computationalModel, executionStatus);
	}
	
}