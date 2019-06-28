package com.uff.phenomanager.repository;

import org.springframework.stereotype.Repository;

import com.uff.phenomanager.domain.ComputationalModel;
import com.uff.phenomanager.domain.ExecutionEnvironment;
import com.uff.phenomanager.repository.core.BaseRepository;

@Repository
public interface ExecutionEnvironmentRepository extends BaseRepository<ExecutionEnvironment> {

	ExecutionEnvironment findByComputationalModelAndActive(ComputationalModel computationalModel, Boolean active);

}