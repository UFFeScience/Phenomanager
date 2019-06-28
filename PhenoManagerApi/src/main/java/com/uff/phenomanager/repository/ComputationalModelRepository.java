package com.uff.phenomanager.repository;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.uff.phenomanager.domain.ComputationalModel;
import com.uff.phenomanager.domain.Experiment;
import com.uff.phenomanager.repository.core.BaseRepository;

@Repository
public interface ComputationalModelRepository extends BaseRepository<ComputationalModel> {
	
	List<ComputationalModel> findAllByExperiment(Experiment experiment);
	
}