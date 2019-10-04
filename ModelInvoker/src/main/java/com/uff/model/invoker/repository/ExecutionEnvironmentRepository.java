package com.uff.model.invoker.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.uff.model.invoker.domain.EnvironmentType;
import com.uff.model.invoker.domain.ExecutionEnvironment;

@Repository
public interface ExecutionEnvironmentRepository extends BaseRepository<ExecutionEnvironment> {
	
	List<ExecutionEnvironment> findByType(EnvironmentType type, Pageable pageable);
	
}