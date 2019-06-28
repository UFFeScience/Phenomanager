package com.uff.model.invoker.repository;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.uff.model.invoker.domain.ComputationalModel;
import com.uff.model.invoker.domain.ModelMetadataExtractor;

@Repository
public interface ModelMetadataExtractorRepository extends BaseRepository<ModelMetadataExtractor> {
	
	List<ModelMetadataExtractor> findAllByComputationalModelAndActive(ComputationalModel computationalModel, Boolean active);
	
}