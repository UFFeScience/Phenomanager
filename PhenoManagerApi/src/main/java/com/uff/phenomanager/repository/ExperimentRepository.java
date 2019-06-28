package com.uff.phenomanager.repository;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.uff.phenomanager.domain.Experiment;
import com.uff.phenomanager.domain.Hypothesis;
import com.uff.phenomanager.domain.Project;
import com.uff.phenomanager.repository.core.BaseRepository;

@Repository
public interface ExperimentRepository extends BaseRepository<Experiment> {
	
	List<Experiment> findAllByHypothesisPhenomenonProject(Project project);

	List<Experiment> findAllByHypothesis(Hypothesis hypothesis);
	
}