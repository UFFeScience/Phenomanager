package com.uff.model.invoker.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.uff.model.invoker.domain.EnvironmentType;
import com.uff.model.invoker.domain.Environment;
import com.uff.model.invoker.domain.ExecutionStatus;
import com.uff.model.invoker.domain.Executor;
import com.uff.model.invoker.domain.Execution;
import com.uff.model.invoker.exception.AbortedExecutionException;
import com.uff.model.invoker.repository.ExecutionRepository;
import com.uff.model.invoker.service.core.ApiRestService;

@Service
public class ExecutionService extends ApiRestService<Execution, ExecutionRepository> {
	
	private static final Logger log = LoggerFactory.getLogger(ExecutionService.class);
	
	@Autowired
	private ExecutionRepository executionRepository;
	
	@Override
	protected ExecutionRepository getRepository() {
		return executionRepository;
	}
	
	@Override
	protected Class<Execution> getEntityClass() {
		return Execution.class;
	}
	
	public List<Execution> findByEnvironmentTypeAndStatus(EnvironmentType type, ExecutionStatus status, Pageable pageable) {
    	return executionRepository.findByEnvironmentTypeAndStatus(type, status, pageable);
	}
	
	public Execution findBySlug(String slug) {
		return executionRepository.findOneBySlug(slug);
	}
	
	public Execution findByExecutorAndEnvironmentAndStatus(Executor executor, Environment environment, ExecutionStatus status) {
		return executionRepository.findByExecutorAndEnvironmentAndStatus(executor, environment, status);
	}
	
	public Execution updateExecutorOutput(Execution execution, String executionLog) throws AbortedExecutionException {
		return updateExecutorOutput(execution, executionLog, Boolean.FALSE);
	}
	
	public Execution updateExecutorOutput(Execution executor, String executionLog, Boolean isKillAction) throws AbortedExecutionException {
		log.info("Updating Execution of slug [{}] with log entry [{}]", executor.getSlug(), executionLog);
		Execution executionFromDb = findBySlug(executor.getSlug());
		
		if (executionFromDb != null) {
			executor.setOutput(executionFromDb.getOutput());
		
			if (executionFromDb.getHasAbortionRequested() != null 
					&& executionFromDb.getHasAbortionRequested().equals(Boolean.TRUE)) {
				
				if (executor.getExecutionMetadataFileId() != null && !"".equals(executor.getExecutionMetadataFileId()) &&
						(executionFromDb.getExecutionMetadataFileId() == null || "".equals(executionFromDb.getExecutionMetadataFileId()))) {
					executionFromDb.setExecutionMetadataFileId(executor.getExecutionMetadataFileId());
					executionFromDb.appendExecutionLog(executionLog);
					super.update(executionFromDb);
				}
				
				if (!isKillAction) {
					throw new AbortedExecutionException("Task was aborted");
				}
			}
			
			if (executor.getHasAbortionRequested() != null 
					&& executor.getHasAbortionRequested().equals(Boolean.TRUE)) {
			
				if (executionFromDb.getExecutionMetadataFileId() != null && !"".equals(executionFromDb.getExecutionMetadataFileId()) &&
						(executor.getExecutionMetadataFileId() == null || "".equals(executor.getExecutionMetadataFileId()))) {
					executor.setExecutionMetadataFileId(executionFromDb.getExecutionMetadataFileId());
				}
			}
		}
		
		executor.appendExecutionLog(executionLog);
		return super.update(executor);
	}
	
	public Execution updateSystemLog(Execution execution, String executionLog) throws AbortedExecutionException {
		return updateSystemLog(execution, executionLog, Boolean.FALSE);
	}
	
	public Execution updateSystemLog(Execution execution, String executionLog, Boolean isKillAction) throws AbortedExecutionException {
		log.info("Updating Execution of slug [{}] with log entry [{}]", execution.getSlug(), executionLog);
		Execution executionFromDb = findBySlug(execution.getSlug());
		
		if (executionFromDb != null) {
			execution.setOutput(executionFromDb.getOutput());
		
			if (executionFromDb.getHasAbortionRequested() != null 
					&& executionFromDb.getHasAbortionRequested().equals(Boolean.TRUE)) {

				if (execution.getExecutionMetadataFileId() != null && !"".equals(execution.getExecutionMetadataFileId()) &&
						(executionFromDb.getExecutionMetadataFileId() == null || "".equals(executionFromDb.getExecutionMetadataFileId()))) {
					executionFromDb.setExecutionMetadataFileId(execution.getExecutionMetadataFileId());
					executionFromDb.appendSystemLog(executionLog);
					super.update(executionFromDb);
				}
				
				if (!isKillAction) {
					throw new AbortedExecutionException("Task was aborted");
				}
			}
			
			if (execution.getHasAbortionRequested() != null 
					&& execution.getHasAbortionRequested().equals(Boolean.TRUE)) {
				
				if (executionFromDb.getExecutionMetadataFileId() != null && !"".equals(executionFromDb.getExecutionMetadataFileId()) &&
						(execution.getExecutionMetadataFileId() == null || "".equals(execution.getExecutionMetadataFileId()))) {
					execution.setExecutionMetadataFileId(executionFromDb.getExecutionMetadataFileId());
				}
			}
		}
		
		execution.appendSystemLog(executionLog);
		return super.update(execution);
	}
	
	public Execution updateSystemLog(Execution execution, String[] executionLog) throws AbortedExecutionException {
		return updateSystemLog(execution, executionLog, Boolean.FALSE);
	}
	
	public Execution updateSystemLog(Execution execution, String[] executionLog, Boolean isKillAction) {
		log.info("Updating Execution of slug [{}] with log entry [{}]", execution.getSlug(), executionLog);
		Execution executionFromDb = findBySlug(execution.getSlug());
		
		if (executionFromDb != null) {
			execution.setOutput(executionFromDb.getOutput());
		
			if (executionFromDb.getHasAbortionRequested() != null 
					&& executionFromDb.getHasAbortionRequested().equals(Boolean.TRUE)) {
				
				if (execution.getExecutionMetadataFileId() != null && !"".equals(execution.getExecutionMetadataFileId()) &&
						(executionFromDb.getExecutionMetadataFileId() == null || "".equals(executionFromDb.getExecutionMetadataFileId()))) {
					executionFromDb.setExecutionMetadataFileId(execution.getExecutionMetadataFileId());
					executionFromDb.appendSystemLogs(executionLog);
					super.update(executionFromDb);
				}
				
				if (!isKillAction) {
					throw new AbortedExecutionException("Task was aborted");
				}
			}
			
			if (execution.getHasAbortionRequested() != null 
					&& execution.getHasAbortionRequested().equals(Boolean.TRUE)) {

				if (executionFromDb.getExecutionMetadataFileId() != null && !"".equals(executionFromDb.getExecutionMetadataFileId()) &&
						(execution.getExecutionMetadataFileId() == null || "".equals(execution.getExecutionMetadataFileId()))) {
					execution.setExecutionMetadataFileId(executionFromDb.getExecutionMetadataFileId());
				}
			}
		}
		
		execution.appendSystemLogs(executionLog);
		return super.update(execution);
	}
	
	@Override
	public Execution update(Execution execution) {
		return update(execution, Boolean.FALSE);
	}

	public Execution update(Execution execution, Boolean isKillAction) {
		Execution executionFromDb = findBySlug(execution.getSlug());
		
		if (executionFromDb != null) {
			
			if (executionFromDb.getHasAbortionRequested() != null 
					&& executionFromDb.getHasAbortionRequested().equals(Boolean.TRUE)) {
				
				if (execution.getExecutionMetadataFileId() != null && !"".equals(execution.getExecutionMetadataFileId()) &&
						(executionFromDb.getExecutionMetadataFileId() == null || "".equals(executionFromDb.getExecutionMetadataFileId()))) {
					executionFromDb.setExecutionMetadataFileId(execution.getExecutionMetadataFileId());
					super.update(executionFromDb);
				}
				
				if (!isKillAction) {
					throw new AbortedExecutionException("Task was aborted");
				}
			}
			
			if (execution.getHasAbortionRequested() != null 
					&& execution.getHasAbortionRequested().equals(Boolean.TRUE)) {
				
				if (executionFromDb.getExecutionMetadataFileId() != null && !"".equals(executionFromDb.getExecutionMetadataFileId()) &&
						(execution.getExecutionMetadataFileId() == null || "".equals(execution.getExecutionMetadataFileId()))) {
					execution.setExecutionMetadataFileId(executionFromDb.getExecutionMetadataFileId());
				}
			}
		}
		
		return super.update(execution);
	}
	
}