package com.uff.model.invoker.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.uff.model.invoker.domain.ModelExecutor;
import com.uff.model.invoker.domain.ModelResultMetadata;
import com.uff.model.invoker.repository.ModelResultMetadataRepository;

@Service
public class ModelResultMetadataService extends ApiRestService<ModelResultMetadata, ModelResultMetadataRepository> {
	
	@Autowired
	private ModelResultMetadataRepository modelResultMetadataRepository;
	
	@Override
	protected ModelResultMetadataRepository getRepository() {
		return modelResultMetadataRepository;
	}
	
	@Override
	protected Class<ModelResultMetadata> getEntityClass() {
		return ModelResultMetadata.class;
	}
	
	public ModelResultMetadata findByModelExecutor(ModelExecutor modelExecutor) {
		return modelResultMetadataRepository.findByModelExecutor(modelExecutor);
	}
	
}