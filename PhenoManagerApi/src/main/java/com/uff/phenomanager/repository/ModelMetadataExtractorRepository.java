package com.uff.phenomanager.repository;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.stereotype.Repository;

import com.uff.phenomanager.domain.ComputationalModel;
import com.uff.phenomanager.domain.ModelMetadataExtractor;
import com.uff.phenomanager.repository.core.BaseRepository;

@Repository
public interface ModelMetadataExtractorRepository extends BaseRepository<ModelMetadataExtractor> {
	
	@Transactional
	Integer deleteByComputationalModel(ComputationalModel computationalModel);

	ModelMetadataExtractor findByComputationalModelAndActive(ComputationalModel computationalModel, Boolean active);

	List<ModelMetadataExtractor> findAllByComputationalModel(ComputationalModel computationalModel);
	
}