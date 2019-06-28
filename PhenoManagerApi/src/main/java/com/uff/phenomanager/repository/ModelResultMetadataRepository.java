package com.uff.phenomanager.repository;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.stereotype.Repository;

import com.uff.phenomanager.domain.ComputationalModel;
import com.uff.phenomanager.domain.ModelResultMetadata;
import com.uff.phenomanager.repository.core.BaseRepository;

@Repository
public interface ModelResultMetadataRepository extends BaseRepository<ModelResultMetadata> {

	@Transactional
	Integer deleteByComputationalModel(ComputationalModel computationalModel);

	List<ModelResultMetadata> findAllByComputationalModel(ComputationalModel computationalModel);
	
}