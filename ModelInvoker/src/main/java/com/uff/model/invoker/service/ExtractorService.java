package com.uff.model.invoker.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.uff.model.invoker.domain.ComputationalModel;
import com.uff.model.invoker.domain.Extractor;
import com.uff.model.invoker.repository.ExtractorRepository;
import com.uff.model.invoker.service.core.ApiRestService;

@Service
public class ExtractorService extends ApiRestService<Extractor, ExtractorRepository> {
	
	@Autowired
	private ExtractorRepository extractorRepository;
	
	@Override
	protected ExtractorRepository getRepository() {
		return extractorRepository;
	}
	
	@Override
	protected Class<Extractor> getEntityClass() {
		return Extractor.class;
	}
	
	public List<Extractor> findAllByComputationalModelAndActive(ComputationalModel computationalModel, Boolean active) {
		return extractorRepository.findAllByComputationalModelAndActive(computationalModel, active);
	}
	
	public List<Extractor> findAllBySlugInAndActive(List<String> slugs, Boolean active) {
		return extractorRepository.findAllBySlugInAndActive(slugs, active);
	}
	
}