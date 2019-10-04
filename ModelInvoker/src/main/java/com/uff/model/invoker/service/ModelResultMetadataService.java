package com.uff.model.invoker.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.uff.model.invoker.domain.EnvironmentType;
import com.uff.model.invoker.domain.ExecutionStatus;
import com.uff.model.invoker.domain.ModelExecutor;
import com.uff.model.invoker.domain.ModelResultMetadata;
import com.uff.model.invoker.exception.AbortedExecutionException;
import com.uff.model.invoker.repository.ModelResultMetadataRepository;

@Service
public class ModelResultMetadataService extends ApiRestService<ModelResultMetadata, ModelResultMetadataRepository> {
	
	private static final Logger log = LoggerFactory.getLogger(ModelResultMetadataService.class);
	
	@Autowired
	private ModelResultMetadataRepository modelResultMetadataRepository;
	
	@Override
	protected ModelResultMetadataRepository getRepository() {
		return modelResultMetadataRepository;
	}
	
	@Override
	protected Class<ModelResultMetadata> getEntityClass() {
		return ModelResultMetadata.class;
	}
	
	public List<ModelResultMetadata> findByExecutionEnvironmentTypeAndExecutionStatus(
			EnvironmentType type, ExecutionStatus executionStatus, Pageable pageable) {
    	return modelResultMetadataRepository.findByExecutionEnvironmentTypeAndExecutionStatus(type, executionStatus, pageable);
	}
	
	public ModelResultMetadata findBySlug(String slug) {
		return modelResultMetadataRepository.findOneBySlug(slug);
	}
	
	public ModelResultMetadata findByModelExecutorAndExecutorExecutionStatus(ModelExecutor modelExecutor, ExecutionStatus executorExecutionStatus) {
		return modelResultMetadataRepository.findByModelExecutorAndExecutorExecutionStatus(modelExecutor, executorExecutionStatus);
	}
	
	public ModelResultMetadata updateExecutionOutput(ModelResultMetadata modelResultMetadata, String executionLog) throws AbortedExecutionException {
		log.info("Updating ModelResultMetadata of slug [{}] with log entry [{}]", modelResultMetadata.getSlug(), executionLog);
		ModelResultMetadata modelResultMetadataFromDb = findBySlug(modelResultMetadata.getSlug());
		
		if (modelResultMetadataFromDb == null || ((ExecutionStatus.ABORTED.equals(modelResultMetadataFromDb.getExecutionStatus()) ||
				ExecutionStatus.ABORTED.equals(modelResultMetadataFromDb.getExecutorExecutionStatus())) && 
				!ExecutionStatus.ABORTED.equals(modelResultMetadata.getExecutorExecutionStatus()) &&
				!ExecutionStatus.ABORTED.equals(modelResultMetadata.getExecutionStatus()))) {
			
			if (modelResultMetadata.getExecutionMetadataFileId() != null && !"".equals(modelResultMetadata.getExecutionMetadataFileId()) &&
					(modelResultMetadataFromDb.getExecutionMetadataFileId() == null || "".equals(modelResultMetadataFromDb.getExecutionMetadataFileId()))) {
				modelResultMetadataFromDb.setExecutionMetadataFileId(modelResultMetadata.getExecutionMetadataFileId());
				super.update(modelResultMetadataFromDb);
			}
			
			throw new AbortedExecutionException("Task was aborted");
		}
		
		modelResultMetadata.appendSystemLog(executionLog);
		return super.update(modelResultMetadata);
	}
	
	@Override
	public ModelResultMetadata update(ModelResultMetadata modelResultMetadata) {
		ModelResultMetadata modelResultMetadataFromDb = findBySlug(modelResultMetadata.getSlug());
		
		if (modelResultMetadataFromDb == null || ((ExecutionStatus.ABORTED.equals(modelResultMetadataFromDb.getExecutionStatus()) ||
				ExecutionStatus.ABORTED.equals(modelResultMetadataFromDb.getExecutorExecutionStatus())) && 
				!ExecutionStatus.ABORTED.equals(modelResultMetadata.getExecutorExecutionStatus()) &&
				!ExecutionStatus.ABORTED.equals(modelResultMetadata.getExecutionStatus()))) {
			
			if (modelResultMetadata.getExecutionMetadataFileId() != null && !"".equals(modelResultMetadata.getExecutionMetadataFileId()) &&
					(modelResultMetadataFromDb.getExecutionMetadataFileId() == null || "".equals(modelResultMetadataFromDb.getExecutionMetadataFileId()))) {
				modelResultMetadataFromDb.setExecutionMetadataFileId(modelResultMetadata.getExecutionMetadataFileId());
				super.update(modelResultMetadataFromDb);
			}
			
			throw new AbortedExecutionException("Task was aborted");
		}
		
		return super.update(modelResultMetadata);
	}
	
}