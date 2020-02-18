package com.uff.model.invoker.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.uff.model.invoker.domain.Environment;
import com.uff.model.invoker.domain.ExecutionStatus;
import com.uff.model.invoker.domain.ExtractorExecution;
import com.uff.model.invoker.domain.Extractor;
import com.uff.model.invoker.exception.AbortedExecutionException;
import com.uff.model.invoker.repository.ExtractorExecutionRepository;
import com.uff.model.invoker.service.core.ApiRestService;

@Service
public class ExtractorExecutionService extends ApiRestService<ExtractorExecution, ExtractorExecutionRepository> {
	
	@Autowired
	private ExtractorExecutionRepository extractorExecutionRepository;
	
	@Override
	protected ExtractorExecutionRepository getRepository() {
		return extractorExecutionRepository;
	}
	
	@Override
	protected Class<ExtractorExecution> getEntityClass() {
		return ExtractorExecution.class;
	}
	
	@Override
	public ExtractorExecution update(ExtractorExecution extractorExecution) {
		ExtractorExecution extractorExecutionFromDb = findBySlug(extractorExecution.getSlug());
		
		if (extractorExecutionFromDb != null && (ExecutionStatus.FINISHED.equals(extractorExecutionFromDb.getStatus()) &&
				ExecutionStatus.ABORTED.equals(extractorExecution.getStatus()))) {
			throw new AbortedExecutionException("Task was aborted");
		}
		
		if (extractorExecutionFromDb != null && (ExecutionStatus.ABORTED.equals(extractorExecutionFromDb.getStatus()) &&
				!ExecutionStatus.ABORTED.equals(extractorExecution.getStatus()))) {
			
			if (extractorExecution.getExecutionMetadataFileId() != null && !"".equals(extractorExecution.getExecutionMetadataFileId()) &&
					(extractorExecutionFromDb.getExecutionMetadataFileId() == null || "".equals(extractorExecutionFromDb.getExecutionMetadataFileId()))) {
				extractorExecutionFromDb.setExecutionMetadataFileId(extractorExecution.getExecutionMetadataFileId());
				return super.update(extractorExecutionFromDb);
			}
		}
		
		return super.update(extractorExecution);
	}

	public ExtractorExecution findByExtractorAndEnvironment(Extractor extractor, Environment environment, ExecutionStatus status) {
		return extractorExecutionRepository
				.findByExtractorAndExecutionEnvironmentAndExecutionStatus(extractor, environment, status);
	}
	
}