package com.uff.model.invoker.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.uff.model.invoker.domain.Executor;
import com.uff.model.invoker.repository.ExecutorRepository;
import com.uff.model.invoker.service.core.ApiRestService;

@Service
public class ExecutorService extends ApiRestService<Executor, ExecutorRepository> {
	
	@Autowired
	private ExecutorRepository executorRepository;
	
	@Override
	protected ExecutorRepository getRepository() {
		return executorRepository;
	}
	
	@Override
	protected Class<Executor> getEntityClass() {
		return Executor.class;
	}
	
}