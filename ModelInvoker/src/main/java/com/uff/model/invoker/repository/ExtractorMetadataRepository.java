package com.uff.model.invoker.repository;

import org.springframework.stereotype.Repository;

import com.uff.model.invoker.domain.ExecutionEnvironment;
import com.uff.model.invoker.domain.ExecutionStatus;
import com.uff.model.invoker.domain.ExtractorMetadata;
import com.uff.model.invoker.domain.ModelMetadataExtractor;

@Repository
public interface ExtractorMetadataRepository extends BaseRepository<ExtractorMetadata> {

	ExtractorMetadata findByModelMetadataExtractorAndModelResultMetadataExecutionEnvironmentAndModelResultMetadataExecutionStatus(
			ModelMetadataExtractor modelMetadataExtractor, ExecutionEnvironment executionEnvironment,
			ExecutionStatus executionStatus);}