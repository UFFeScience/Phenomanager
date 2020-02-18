package com.uff.model.invoker.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.uff.model.invoker.domain.Environment;
import com.uff.model.invoker.repository.EnvironmentRepository;
import com.uff.model.invoker.service.core.ApiRestService;

@Service
public class EnvironmentService extends ApiRestService<Environment, EnvironmentRepository> {
	
	@Autowired
	private EnvironmentRepository environmentRepository;
	
	@Override
	protected EnvironmentRepository getRepository() {
		return environmentRepository;
	}
	
	@Override
	protected Class<Environment> getEntityClass() {
		return Environment.class;
	}
	
}