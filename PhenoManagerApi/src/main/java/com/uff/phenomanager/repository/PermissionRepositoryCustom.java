package com.uff.phenomanager.repository;

import com.uff.phenomanager.domain.Permission;
import com.uff.phenomanager.domain.User;

public interface PermissionRepositoryCustom {
	
	Permission findOneByUserAndEntityNameAndEntitySlug(User user, String entityName, String entitySlug);

}