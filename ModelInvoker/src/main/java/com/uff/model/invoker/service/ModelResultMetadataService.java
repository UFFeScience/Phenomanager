package com.uff.model.invoker.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.uff.model.invoker.domain.EnvironmentType;
import com.uff.model.invoker.domain.ExecutionEnvironment;
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
	
	public ModelResultMetadata findByModelExecutorAndExecutionEnvironmentAndExecutionStatus(
			ModelExecutor modelExecutor, ExecutionEnvironment executionEnvironment, ExecutionStatus executionStatus) {
		return modelResultMetadataRepository.findByModelExecutorAndExecutionEnvironmentAndExecutionStatus(modelExecutor, executionEnvironment, executionStatus);
	}
	
	public ModelResultMetadata updateExecutorOutput(ModelResultMetadata modelResultMetadata, String executionLog) throws AbortedExecutionException {
		return updateExecutorOutput(modelResultMetadata, executionLog, Boolean.FALSE);
	}
	
	public ModelResultMetadata updateExecutorOutput(ModelResultMetadata modelResultMetadata, String executionLog, Boolean isKillAction) throws AbortedExecutionException {
		log.info("Updating ModelResultMetadata of slug [{}] with log entry [{}]", modelResultMetadata.getSlug(), executionLog);
		ModelResultMetadata modelResultMetadataFromDb = findBySlug(modelResultMetadata.getSlug());
		
		if (modelResultMetadataFromDb != null) {
			modelResultMetadata.setExecutionOutput(modelResultMetadataFromDb.getExecutionOutput());
		
			if (modelResultMetadataFromDb.getHasAbortRequested() != null 
					&& modelResultMetadataFromDb.getHasAbortRequested().equals(Boolean.TRUE)) {
				
				if (modelResultMetadata.getExecutionMetadataFileId() != null && !"".equals(modelResultMetadata.getExecutionMetadataFileId()) &&
						(modelResultMetadataFromDb.getExecutionMetadataFileId() == null || "".equals(modelResultMetadataFromDb.getExecutionMetadataFileId()))) {
					modelResultMetadataFromDb.setExecutionMetadataFileId(modelResultMetadata.getExecutionMetadataFileId());
					modelResultMetadataFromDb.appendExecutionLog(executionLog);
					super.update(modelResultMetadataFromDb);
				}
				
				if (!isKillAction) {
					throw new AbortedExecutionException("Task was aborted");
				}
			}
			
			if (modelResultMetadata.getHasAbortRequested() != null 
					&& modelResultMetadata.getHasAbortRequested().equals(Boolean.TRUE)) {
			
				if (modelResultMetadataFromDb.getExecutionMetadataFileId() != null && !"".equals(modelResultMetadataFromDb.getExecutionMetadataFileId()) &&
						(modelResultMetadata.getExecutionMetadataFileId() == null || "".equals(modelResultMetadata.getExecutionMetadataFileId()))) {
					modelResultMetadata.setExecutionMetadataFileId(modelResultMetadataFromDb.getExecutionMetadataFileId());
				}
			}
		}
		
		modelResultMetadata.appendExecutionLog(executionLog);
		return super.update(modelResultMetadata);
	}
	
	public ModelResultMetadata updateSystemLog(ModelResultMetadata modelResultMetadata, String executionLog) throws AbortedExecutionException {
		return updateSystemLog(modelResultMetadata, executionLog, Boolean.FALSE);
	}
	
	public ModelResultMetadata updateSystemLog(ModelResultMetadata modelResultMetadata, String executionLog, Boolean isKillAction) throws AbortedExecutionException {
		log.info("Updating ModelResultMetadata of slug [{}] with log entry [{}]", modelResultMetadata.getSlug(), executionLog);
		ModelResultMetadata modelResultMetadataFromDb = findBySlug(modelResultMetadata.getSlug());
		
		if (modelResultMetadataFromDb != null) {
			modelResultMetadata.setExecutionOutput(modelResultMetadataFromDb.getExecutionOutput());
		
			if (modelResultMetadataFromDb.getHasAbortRequested() != null 
					&& modelResultMetadataFromDb.getHasAbortRequested().equals(Boolean.TRUE)) {

				if (modelResultMetadata.getExecutionMetadataFileId() != null && !"".equals(modelResultMetadata.getExecutionMetadataFileId()) &&
						(modelResultMetadataFromDb.getExecutionMetadataFileId() == null || "".equals(modelResultMetadataFromDb.getExecutionMetadataFileId()))) {
					modelResultMetadataFromDb.setExecutionMetadataFileId(modelResultMetadata.getExecutionMetadataFileId());
					modelResultMetadataFromDb.appendSystemLog(executionLog);
					super.update(modelResultMetadataFromDb);
				}
				
				if (!isKillAction) {
					throw new AbortedExecutionException("Task was aborted");
				}
			}
			
			if (modelResultMetadata.getHasAbortRequested() != null 
					&& modelResultMetadata.getHasAbortRequested().equals(Boolean.TRUE)) {
				
				if (modelResultMetadataFromDb.getExecutionMetadataFileId() != null && !"".equals(modelResultMetadataFromDb.getExecutionMetadataFileId()) &&
						(modelResultMetadata.getExecutionMetadataFileId() == null || "".equals(modelResultMetadata.getExecutionMetadataFileId()))) {
					modelResultMetadata.setExecutionMetadataFileId(modelResultMetadataFromDb.getExecutionMetadataFileId());
				}
			}
		}
		
		modelResultMetadata.appendSystemLog(executionLog);
		return super.update(modelResultMetadata);
	}
	
	public ModelResultMetadata updateSystemLog(ModelResultMetadata modelResultMetadata, String[] executionLog) throws AbortedExecutionException {
		return updateSystemLog(modelResultMetadata, executionLog, Boolean.FALSE);
	}
	
	public ModelResultMetadata updateSystemLog(ModelResultMetadata modelResultMetadata, String[] executionLog, Boolean isKillAction) {
		log.info("Updating ModelResultMetadata of slug [{}] with log entry [{}]", modelResultMetadata.getSlug(), executionLog);
		ModelResultMetadata modelResultMetadataFromDb = findBySlug(modelResultMetadata.getSlug());
		
		if (modelResultMetadataFromDb != null) {
			modelResultMetadata.setExecutionOutput(modelResultMetadataFromDb.getExecutionOutput());
		
			if (modelResultMetadataFromDb.getHasAbortRequested() != null 
					&& modelResultMetadataFromDb.getHasAbortRequested().equals(Boolean.TRUE)) {
				
				if (modelResultMetadata.getExecutionMetadataFileId() != null && !"".equals(modelResultMetadata.getExecutionMetadataFileId()) &&
						(modelResultMetadataFromDb.getExecutionMetadataFileId() == null || "".equals(modelResultMetadataFromDb.getExecutionMetadataFileId()))) {
					modelResultMetadataFromDb.setExecutionMetadataFileId(modelResultMetadata.getExecutionMetadataFileId());
					modelResultMetadataFromDb.appendSystemLogs(executionLog);
					super.update(modelResultMetadataFromDb);
				}
				
				if (!isKillAction) {
					throw new AbortedExecutionException("Task was aborted");
				}
			}
			
			if (modelResultMetadata.getHasAbortRequested() != null 
					&& modelResultMetadata.getHasAbortRequested().equals(Boolean.TRUE)) {

				if (modelResultMetadataFromDb.getExecutionMetadataFileId() != null && !"".equals(modelResultMetadataFromDb.getExecutionMetadataFileId()) &&
						(modelResultMetadata.getExecutionMetadataFileId() == null || "".equals(modelResultMetadata.getExecutionMetadataFileId()))) {
					modelResultMetadata.setExecutionMetadataFileId(modelResultMetadataFromDb.getExecutionMetadataFileId());
				}
			}
		}
		
		modelResultMetadata.appendSystemLogs(executionLog);
		return super.update(modelResultMetadata);
	}
	
	@Override
	public ModelResultMetadata update(ModelResultMetadata modelResultMetadata) {
		return update(modelResultMetadata, Boolean.FALSE);
	}

	public ModelResultMetadata update(ModelResultMetadata modelResultMetadata, Boolean isKillAction) {
		ModelResultMetadata modelResultMetadataFromDb = findBySlug(modelResultMetadata.getSlug());
		
		if (modelResultMetadataFromDb != null) {
			
			if (modelResultMetadataFromDb.getHasAbortRequested() != null 
					&& modelResultMetadataFromDb.getHasAbortRequested().equals(Boolean.TRUE)) {
				
				if (modelResultMetadata.getExecutionMetadataFileId() != null && !"".equals(modelResultMetadata.getExecutionMetadataFileId()) &&
						(modelResultMetadataFromDb.getExecutionMetadataFileId() == null || "".equals(modelResultMetadataFromDb.getExecutionMetadataFileId()))) {
					modelResultMetadataFromDb.setExecutionMetadataFileId(modelResultMetadata.getExecutionMetadataFileId());
					super.update(modelResultMetadataFromDb);
				}
				
				if (!isKillAction) {
					throw new AbortedExecutionException("Task was aborted");
				}
			}
			
			if (modelResultMetadata.getHasAbortRequested() != null 
					&& modelResultMetadata.getHasAbortRequested().equals(Boolean.TRUE)) {
				
				if (modelResultMetadataFromDb.getExecutionMetadataFileId() != null && !"".equals(modelResultMetadataFromDb.getExecutionMetadataFileId()) &&
						(modelResultMetadata.getExecutionMetadataFileId() == null || "".equals(modelResultMetadata.getExecutionMetadataFileId()))) {
					modelResultMetadata.setExecutionMetadataFileId(modelResultMetadataFromDb.getExecutionMetadataFileId());
				}
			}
		}
		
		return super.update(modelResultMetadata);
	}
	
}