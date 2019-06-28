package com.uff.model.invoker.repository;

import com.uff.model.invoker.domain.Permission;
import com.uff.model.invoker.domain.User;

public interface PermissionRepositoryCustom {
	
	Permission findOneByUserAndEntityNameAndComputationalModelSlug(User user, String computationalModelSlug);

}