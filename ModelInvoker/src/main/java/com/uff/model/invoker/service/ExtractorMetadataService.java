package com.uff.model.invoker.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.uff.model.invoker.domain.ExecutionEnvironment;
import com.uff.model.invoker.domain.ExecutionStatus;
import com.uff.model.invoker.domain.ExtractorMetadata;
import com.uff.model.invoker.domain.ModelMetadataExtractor;
import com.uff.model.invoker.exception.AbortedExecutionException;
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
	
	@Override
	public ExtractorMetadata update(ExtractorMetadata extractorMetadata) {
		ExtractorMetadata extractorMetadataFromDb = findBySlug(extractorMetadata.getSlug());
		
		if (extractorMetadataFromDb != null && (ExecutionStatus.FINISHED.equals(extractorMetadataFromDb.getExecutionStatus()) &&
				ExecutionStatus.ABORTED.equals(extractorMetadata.getExecutionStatus()))) {
			throw new AbortedExecutionException("Task was aborted");
		}
		
		if (extractorMetadataFromDb != null && (ExecutionStatus.ABORTED.equals(extractorMetadataFromDb.getExecutionStatus()) &&
				!ExecutionStatus.ABORTED.equals(extractorMetadata.getExecutionStatus()))) {
			
			if (extractorMetadata.getExecutionMetadataFileId() != null && !"".equals(extractorMetadata.getExecutionMetadataFileId()) &&
					(extractorMetadataFromDb.getExecutionMetadataFileId() == null || "".equals(extractorMetadataFromDb.getExecutionMetadataFileId()))) {
				extractorMetadataFromDb.setExecutionMetadataFileId(extractorMetadata.getExecutionMetadataFileId());
				return super.update(extractorMetadataFromDb);
			}
		}
		
		return super.update(extractorMetadata);
	}

	public ExtractorMetadata findByModelMetadataExtractorAndExecutionEnvironment(ModelMetadataExtractor modelMetadataExtractor, 
			ExecutionEnvironment executionEnvironment, ExecutionStatus executionStatus) {
		return extractorMetadataRepository
				.findByModelMetadataExtractorAndModelResultMetadataExecutionEnvironmentAndModelResultMetadataExecutionStatus(
						modelMetadataExtractor, executionEnvironment, executionStatus);
	}
	
}