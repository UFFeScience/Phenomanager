package com.uff.phenomanager.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.uff.phenomanager.Constants.CONTROLLER;
import com.uff.phenomanager.controller.core.ApiRestController;
import com.uff.phenomanager.domain.Permission;
import com.uff.phenomanager.service.PermissionService;

@RestController
@RequestMapping(CONTROLLER.PERMISSION.PATH)
public class PermissionController extends ApiRestController<Permission, PermissionService> {
	
	@Autowired
	private PermissionService permissionService;
	
	@Override
	public PermissionService getService() {
		return permissionService;
	}
	
}