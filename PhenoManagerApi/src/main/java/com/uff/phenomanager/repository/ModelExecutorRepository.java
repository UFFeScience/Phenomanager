package com.uff.phenomanager.repository;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.stereotype.Repository;

import com.uff.phenomanager.domain.ComputationalModel;
import com.uff.phenomanager.domain.ExecutionStatus;
import com.uff.phenomanager.domain.ModelExecutor;
import com.uff.phenomanager.repository.core.BaseRepository;

@Repository
public interface ModelExecutorRepository extends BaseRepository<ModelExecutor> {
	
	ModelExecutor findByComputationalModelAndActive(ComputationalModel computationalModel, Boolean active);
	
	List<ModelExecutor> findAllByComputationalModelAndExecutionStatus(ComputationalModel computationalModel, 
			ExecutionStatus executionStatus);
	
	List<ModelExecutor> findAllByComputationalModel(ComputationalModel computationalModel);

	@Transactional
	Integer deleteByComputationalModel(ComputationalModel computationalModel);
	
}