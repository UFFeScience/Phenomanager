package com.uff.phenomanager.repository;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.stereotype.Repository;

import com.uff.phenomanager.domain.ComputationalModel;
import com.uff.phenomanager.domain.Environment;
import com.uff.phenomanager.domain.ExecutionStatus;
import com.uff.phenomanager.domain.Executor;
import com.uff.phenomanager.domain.Execution;
import com.uff.phenomanager.repository.core.BaseRepository;

@Repository
public interface ExecutionRepository extends BaseRepository<Execution> {

	@Transactional
	Integer deleteByComputationalModel(ComputationalModel computationalModel);

	List<Execution> findAllByComputationalModel(ComputationalModel computationalModel);
	
	Long countByEnvironmentAndStatus(Environment environment, ExecutionStatus status);

	Long countByExecutorAndStatus(Executor executor, ExecutionStatus status);

	Long countByExecutorAndEnvironmentAndStatus(Executor executor, Environment environment, ExecutionStatus status);
	
}