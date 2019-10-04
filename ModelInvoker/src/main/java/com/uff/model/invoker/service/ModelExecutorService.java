package com.uff.model.invoker.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.uff.model.invoker.domain.ExecutionStatus;
import com.uff.model.invoker.domain.ModelExecutor;
import com.uff.model.invoker.exception.AbortedExecutionException;
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
	
	@Override
	public ModelExecutor update(ModelExecutor modelExecutor) {
		ModelExecutor modelExecutorFromDb = findBySlug(modelExecutor.getSlug());
		
		if (modelExecutor == null || (!ExecutionStatus.RUNNING.equals(modelExecutorFromDb.getExecutionStatus()) && 
				!ExecutionStatus.SCHEDULED.equals(modelExecutorFromDb.getExecutionStatus()) && 
				ExecutionStatus.RUNNING.equals(modelExecutor.getExecutionStatus()))) {
			throw new AbortedExecutionException("Task was aborted");
		}
		
		return super.update(modelExecutor);
	}
	
}