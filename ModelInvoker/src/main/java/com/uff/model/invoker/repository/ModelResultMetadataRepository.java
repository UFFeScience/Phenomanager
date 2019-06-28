package com.uff.model.invoker.repository;

import org.springframework.stereotype.Repository;

import com.uff.model.invoker.domain.ModelExecutor;
import com.uff.model.invoker.domain.ModelResultMetadata;

@Repository
public interface ModelResultMetadataRepository extends BaseRepository<ModelResultMetadata> {
	
	ModelResultMetadata findByModelExecutor(ModelExecutor modelExecutor);
	
}