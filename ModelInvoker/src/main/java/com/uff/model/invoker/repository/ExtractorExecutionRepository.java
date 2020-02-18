package com.uff.model.invoker.repository;

import org.springframework.stereotype.Repository;

import com.uff.model.invoker.domain.Environment;
import com.uff.model.invoker.domain.ExecutionStatus;
import com.uff.model.invoker.domain.ExtractorExecution;
import com.uff.model.invoker.repository.core.BaseRepository;
import com.uff.model.invoker.domain.Extractor;

@Repository
public interface ExtractorExecutionRepository extends BaseRepository<ExtractorExecution> {

	ExtractorExecution findByExtractorAndExecutionEnvironmentAndExecutionStatus(Extractor extractor, Environment environment, ExecutionStatus status);
	
}