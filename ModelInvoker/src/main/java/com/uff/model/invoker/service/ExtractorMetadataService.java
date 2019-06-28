package com.uff.model.invoker.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.uff.model.invoker.domain.ExtractorMetadata;
import com.uff.model.invoker.repository.ExtractorMetadataRepository;

@Service
public class ExtractorMetadataService extends ApiRestService<ExtractorMetadata, ExtractorMetadataRepository> {
	
	@Autowired
	private ExtractorMetadataRepository extractorMetadataRepository;
	
	@Override
	protected ExtractorMetadataRepository getRepository() {
		return extractorMetadataRepository;
	}
	
	@Override
	protected Class<ExtractorMetadata> getEntityClass() {
		return ExtractorMetadata.class;
	}
	
}