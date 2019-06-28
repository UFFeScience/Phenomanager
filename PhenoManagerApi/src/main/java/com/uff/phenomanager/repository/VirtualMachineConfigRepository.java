package com.uff.phenomanager.repository;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.uff.phenomanager.domain.ExecutionEnvironment;
import com.uff.phenomanager.domain.VirtualMachineConfig;
import com.uff.phenomanager.repository.core.BaseRepository;

@Repository
public interface VirtualMachineConfigRepository extends BaseRepository<VirtualMachineConfig> {
	
	@Transactional
	Integer deleteByExecutionEnvironment(ExecutionEnvironment executionEnvironment);
	
}