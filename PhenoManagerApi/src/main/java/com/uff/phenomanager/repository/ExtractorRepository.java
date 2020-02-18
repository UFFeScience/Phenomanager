package com.uff.phenomanager.repository;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.stereotype.Repository;

import com.uff.phenomanager.domain.ComputationalModel;
import com.uff.phenomanager.domain.Extractor;
import com.uff.phenomanager.repository.core.BaseRepository;

@Repository
public interface ExtractorRepository extends BaseRepository<Extractor> {
	
	@Transactional
	Integer deleteByComputationalModel(ComputationalModel computationalModel);

	List<Extractor> findAllByComputationalModel(ComputationalModel computationalModel);
	
}