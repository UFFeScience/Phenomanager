package com.uff.phenomanager.repository;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.stereotype.Repository;

import com.uff.phenomanager.domain.Experiment;
import com.uff.phenomanager.domain.ValidationItem;
import com.uff.phenomanager.repository.core.BaseRepository;

@Repository
public interface ValidationItemRepository extends BaseRepository<ValidationItem> {

	List<ValidationItem> findAllByExperiment(Experiment experiment);
	
	@Transactional
	Integer deleteByExperiment(Experiment experiment);
	
}