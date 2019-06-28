package com.uff.model.invoker.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.uff.model.invoker.domain.ComputationalModel;
import com.uff.model.invoker.domain.EnvironmentType;
import com.uff.model.invoker.domain.ExecutionEnvironment;
import com.uff.model.invoker.repository.ExecutionEnvironmentRepository;

@Service
public class ExecutionEnvironmentService extends ApiRestService<ExecutionEnvironment, ExecutionEnvironmentRepository> {
	
	@Autowired
	private ExecutionEnvironmentRepository executionEnvironmentRepository;
	
	@Override
	protected ExecutionEnvironmentRepository getRepository() {
		return executionEnvironmentRepository;
	}
	
	@Override
	protected Class<ExecutionEnvironment> getEntityClass() {
		return ExecutionEnvironment.class;
	}
	
	public ExecutionEnvironment findByComputationalModelAndActive(ComputationalModel computationalModel, Boolean active) {
    	return executionEnvironmentRepository.findByComputationalModelAndActive(computationalModel, active);
	}
	
    public List<ExecutionEnvironment> findByType(EnvironmentType type, Pageable pageable) {
    	return executionEnvironmentRepository.findByType(type, pageable);
	}
    
}