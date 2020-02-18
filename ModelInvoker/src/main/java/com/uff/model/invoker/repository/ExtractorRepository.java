package com.uff.model.invoker.repository;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.uff.model.invoker.domain.ComputationalModel;
import com.uff.model.invoker.domain.Extractor;
import com.uff.model.invoker.repository.core.BaseRepository;

@Repository
public interface ExtractorRepository extends BaseRepository<Extractor> {
	
	List<Extractor> findAllByComputationalModelAndActive(ComputationalModel computationalModel, Boolean active);
	
	List<Extractor> findAllBySlugInAndActive(List<String> slugs, Boolean active);
	
}