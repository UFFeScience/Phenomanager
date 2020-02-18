package com.uff.phenomanager.repository;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.uff.phenomanager.domain.Environment;
import com.uff.phenomanager.domain.VirtualMachine;
import com.uff.phenomanager.repository.core.BaseRepository;

@Repository
public interface VirtualMachineRepository extends BaseRepository<VirtualMachine> {
	
	@Transactional
	Integer deleteByEnvironment(Environment environment);
	
}