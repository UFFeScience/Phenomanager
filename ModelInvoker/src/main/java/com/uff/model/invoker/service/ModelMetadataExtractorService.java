package com.uff.model.invoker.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.uff.model.invoker.domain.ComputationalModel;
import com.uff.model.invoker.domain.ExecutionStatus;
import com.uff.model.invoker.domain.ModelMetadataExtractor;
import com.uff.model.invoker.repository.ModelMetadataExtractorRepository;

@Service
public class ModelMetadataExtractorService extends ApiRestService<ModelMetadataExtractor, ModelMetadataExtractorRepository> {
	
	@Autowired
	private ModelMetadataExtractorRepository modelMetadataExtractorRepository;
	
	@Override
	protected ModelMetadataExtractorRepository getRepository() {
		return modelMetadataExtractorRepository;
	}
	
	@Override
	protected Class<ModelMetadataExtractor> getEntityClass() {
		return ModelMetadataExtractor.class;
	}
	
	public List<ModelMetadataExtractor> findAllByComputationalModelAndActiveAndExecutionStatusNot(
			ComputationalModel computationalModel, Boolean active, ExecutionStatus executionStatus) {
		return modelMetadataExtractorRepository.findAllByComputationalModelAndActiveAndExecutionStatusNot(
				computationalModel, active, executionStatus);
	}
	
	public List<ModelMetadataExtractor> findAllBySlugInAndActive(List<String> slugs, Boolean active) {
		return modelMetadataExtractorRepository.findAllBySlugInAndActive(slugs, active);
	}
	
}