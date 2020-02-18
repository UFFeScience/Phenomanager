package com.uff.phenomanager.repository;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.stereotype.Repository;

import com.uff.phenomanager.domain.ComputationalModel;
import com.uff.phenomanager.domain.Executor;
import com.uff.phenomanager.repository.core.BaseRepository;

@Repository
public interface ExecutorRepository extends BaseRepository<Executor> {
	
	List<Executor> findAllByComputationalModel(ComputationalModel computationalModel);

	@Transactional
	Integer deleteByComputationalModel(ComputationalModel computationalModel);
	
}