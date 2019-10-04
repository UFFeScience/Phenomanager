package com.uff.model.invoker.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.uff.model.invoker.domain.EnvironmentType;
import com.uff.model.invoker.domain.ExecutionStatus;
import com.uff.model.invoker.domain.ModelExecutor;
import com.uff.model.invoker.domain.ModelResultMetadata;

@Repository
public interface ModelResultMetadataRepository extends BaseRepository<ModelResultMetadata> {
	
	List<ModelResultMetadata> findByExecutionEnvironmentTypeAndExecutionStatus(EnvironmentType type,
			ExecutionStatus executionStatus, Pageable pageable);
	
	ModelResultMetadata findByModelExecutorAndExecutorExecutionStatus(ModelExecutor modelExecutor, ExecutionStatus executorExecutionStatus);
	
}