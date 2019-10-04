package com.uff.model.invoker.repository;

import org.springframework.stereotype.Repository;

import com.uff.model.invoker.domain.ComputationalModel;
import com.uff.model.invoker.domain.ExecutionStatus;
import com.uff.model.invoker.domain.ModelExecutor;

@Repository
public interface ModelExecutorRepository extends BaseRepository<ModelExecutor> {
	
	ModelExecutor findByComputationalModelAndExecutionStatus(ComputationalModel computationalModel, 
			ExecutionStatus executionStatus);

}