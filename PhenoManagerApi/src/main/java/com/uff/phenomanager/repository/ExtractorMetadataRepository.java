package com.uff.phenomanager.repository;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.uff.phenomanager.domain.ExecutionEnvironment;
import com.uff.phenomanager.domain.ExecutionStatus;
import com.uff.phenomanager.domain.ExtractorMetadata;
import com.uff.phenomanager.domain.ModelMetadataExtractor;
import com.uff.phenomanager.domain.ModelResultMetadata;
import com.uff.phenomanager.repository.core.BaseRepository;

@Repository
public interface ExtractorMetadataRepository extends BaseRepository<ExtractorMetadata> {

	List<ExtractorMetadata> findAllByModelResultMetadata(ModelResultMetadata modelResultMetadata);
	
	ExtractorMetadata findByModelMetadataExtractorAndExecutionStatus(ModelMetadataExtractor modelMetadataExtractor, ExecutionStatus executionStatus);
	
	Integer deleteByModelResultMetadata(ModelResultMetadata modelResultMetadata);

	Long countByModelMetadataExtractorAndModelResultMetadataExecutionStatus(
			ModelMetadataExtractor modelMetadataExtractor, ExecutionStatus executionStatus);

	Long countByModelMetadataExtractorAndModelResultMetadataExecutionEnvironmentAndModelResultMetadataExecutionStatus(
			ModelMetadataExtractor modelMetadataExtractor, ExecutionEnvironment executionEnvironment,
			ExecutionStatus executionStatus);
	
}