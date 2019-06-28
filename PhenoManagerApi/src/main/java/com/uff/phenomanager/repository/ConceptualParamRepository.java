package com.uff.phenomanager.repository;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.stereotype.Repository;

import com.uff.phenomanager.domain.ConceptualParam;
import com.uff.phenomanager.domain.Experiment;
import com.uff.phenomanager.repository.core.BaseRepository;

@Repository
public interface ConceptualParamRepository extends BaseRepository<ConceptualParam> {

	@Transactional
	Integer deleteByExperiment(Experiment experiment);

	List<ConceptualParam> findByExperiment(Experiment experiment);
	
}