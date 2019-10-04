package com.uff.model.invoker.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
	
}