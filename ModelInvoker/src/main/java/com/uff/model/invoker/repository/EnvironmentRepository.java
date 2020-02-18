package com.uff.model.invoker.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.uff.model.invoker.domain.EnvironmentType;
import com.uff.model.invoker.repository.core.BaseRepository;
import com.uff.model.invoker.domain.Environment;

@Repository
public interface EnvironmentRepository extends BaseRepository<Environment> {
	
	List<Environment> findByType(EnvironmentType type, Pageable pageable);
	
}