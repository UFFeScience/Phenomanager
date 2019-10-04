package com.uff.model.invoker.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.uff.model.invoker.domain.ExecutionStatus;
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
	
	@Override
	public ExtractorMetadata update(ExtractorMetadata extractorMetadata) {
		ExtractorMetadata extractorMetadataFromDb = findBySlug(extractorMetadata.getSlug());
		
		if (extractorMetadataFromDb == null || (ExecutionStatus.FINISHED.equals(extractorMetadataFromDb.getExecutionStatus()) &&
				ExecutionStatus.ABORTED.equals(extractorMetadata.getExecutionStatus()))) {
			
			extractorMetadata.setExecutionStatus(ExecutionStatus.FINISHED);
			
			if (extractorMetadataFromDb.getExecutionMetadataFileId() != null && !"".equals(extractorMetadataFromDb.getExecutionMetadataFileId()) &&
					(extractorMetadataFromDb.getExecutionMetadataFileId() == null || "".equals(extractorMetadataFromDb.getExecutionMetadataFileId()))) {
				extractorMetadataFromDb.setExecutionMetadataFileId(extractorMetadataFromDb.getExecutionMetadataFileId());
				return super.update(extractorMetadataFromDb);
			}
		}
		
		return super.update(extractorMetadata);
	}
	
}