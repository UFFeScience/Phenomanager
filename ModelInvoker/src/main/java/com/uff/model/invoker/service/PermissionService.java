package com.uff.model.invoker.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.uff.model.invoker.domain.Permission;
import com.uff.model.invoker.domain.User;
import com.uff.model.invoker.repository.PermissionRepository;
import com.uff.model.invoker.service.core.ApiRestService;

@Service
public class PermissionService extends ApiRestService<Permission, PermissionRepository> {
	
	@Autowired
	private PermissionRepository permissionRepository;
	
	@Override
	protected PermissionRepository getRepository() {
		return permissionRepository;
	}
	
	@Override
	protected Class<Permission> getEntityClass() {
		return Permission.class;
	}
	
	public Permission findOneByUserAndEntityNameAndComputationalModelSlug(User user, String computationalModelSlug) {
		return permissionRepository.findOneByUserAndEntityNameAndComputationalModelSlug(user, computationalModelSlug);
	}
	
}