package com.uff.phenomanager.repository;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.stereotype.Repository;

import com.uff.phenomanager.domain.Experiment;
import com.uff.phenomanager.domain.Phase;
import com.uff.phenomanager.repository.core.BaseRepository;

@Repository
public interface PhaseRepository extends BaseRepository<Phase> {

	@Transactional
	Integer deleteByExperiment(Experiment experiment);

	List<Phase> findAllByExperiment(Experiment experiment);
	
}