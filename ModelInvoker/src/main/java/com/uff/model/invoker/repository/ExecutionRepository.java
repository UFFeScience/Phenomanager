package com.uff.model.invoker.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.uff.model.invoker.domain.EnvironmentType;
import com.uff.model.invoker.domain.Environment;
import com.uff.model.invoker.domain.ExecutionStatus;
import com.uff.model.invoker.domain.Executor;
import com.uff.model.invoker.repository.core.BaseRepository;
import com.uff.model.invoker.domain.Execution;

@Repository
public interface ExecutionRepository extends BaseRepository<Execution> {
	
	List<Execution> findByEnvironmentTypeAndStatus(EnvironmentType type, ExecutionStatus status, Pageable pageable);

	Execution findByExecutorAndEnvironmentAndStatus(Executor executor, Environment environment, ExecutionStatus status);
	
}