package com.uff.phenomanager.repository;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.uff.phenomanager.domain.Environment;
import com.uff.phenomanager.domain.ExecutionStatus;
import com.uff.phenomanager.domain.ExtractorExecution;
import com.uff.phenomanager.domain.Extractor;
import com.uff.phenomanager.domain.Execution;
import com.uff.phenomanager.repository.core.BaseRepository;

@Repository
public interface ExtractorExecutionRepository extends BaseRepository<ExtractorExecution> {

	List<ExtractorExecution> findAllByExecution(Execution execution);
	
	ExtractorExecution findByExtractorAndExecutionStatus(Extractor extractor, ExecutionStatus status);
	
	Integer deleteByExecution(Execution execution);

	Long countByExtractorAndExecutionStatus(Extractor extractor, ExecutionStatus status);

	Long countByExtractorAndExecutionEnvironmentAndExecutionStatus(Extractor extractor, Environment environment, ExecutionStatus status);
	
}