package com.uff.phenomanager.service.core;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.uff.phenomanager.Constants;
import com.uff.phenomanager.domain.Permission;
import com.uff.phenomanager.domain.PermissionRole;
import com.uff.phenomanager.domain.Role;
import com.uff.phenomanager.domain.User;
import com.uff.phenomanager.domain.core.ApiMetadata;
import com.uff.phenomanager.domain.core.ApiResponse;
import com.uff.phenomanager.domain.core.BaseApiEntity;
import com.uff.phenomanager.domain.core.filter.RequestFilter;
import com.uff.phenomanager.exception.ApiException;
import com.uff.phenomanager.exception.BadRequestApiException;
import com.uff.phenomanager.exception.NotFoundApiException;
import com.uff.phenomanager.repository.core.ApiFilterPermissionRepository;
import com.uff.phenomanager.repository.core.BaseRepository;
import com.uff.phenomanager.service.PermissionService;
import com.uff.phenomanager.service.UserService;
import com.uff.phenomanager.util.ReflectionUtils;
import com.uff.phenomanager.util.TokenUtils;

@Service
public abstract class ApiPermissionRestService<ENTITY extends BaseApiEntity, REPOSITORY extends BaseRepository<ENTITY>> extends ApiRestService<ENTITY, REPOSITORY> {
	
	@Autowired
	private ApiFilterPermissionRepository<ENTITY> apiFilterPermissionRepository;
	
	@Autowired
	private UserService userService;
	
	@Autowired
	protected PermissionService permissionService;
	
	public ApiResponse<ENTITY> findAll(RequestFilter requestFilter, String authorization) throws ApiException {
		ApiResponse<ENTITY> response = new ApiResponse<ENTITY>();
		List<ENTITY> records = findAllRecords(requestFilter, authorization);
		response.setRecords(records);
		
		ApiMetadata metadata = new ApiMetadata();
		metadata.setTotalCount(countAll(requestFilter, authorization));
		metadata.setPageOffset(requestFilter.getFetchOffset());
		
		if (requestFilter.hasValidAggregateFunction()) {
			metadata.setPageSize(records.size());
		} else {
			metadata.setPageSize(requestFilter.getFetchLimit());
		}
		
		response.setMetadata(metadata);
		
		return response;
	}
	
	public Long countAll(RequestFilter requestFilter, String authorization) throws ApiException {
		String token = TokenUtils.getTokenFromAuthorizationHeader(authorization);
		String slugUser = tokenAuthenticationService.getTokenClaim(token, Constants.JWT_AUTH.CLAIM_USER_SLUG);
		
		return apiFilterPermissionRepository.countAll(getEntityClass(), getPermissionEntityName(), requestFilter, slugUser);
	}
	
	public List<ENTITY> findAllRecords(RequestFilter requestFilter, String authorization) throws ApiException {
		String token = TokenUtils.getTokenFromAuthorizationHeader(authorization);
		String slugUser = tokenAuthenticationService.getTokenClaim(token, Constants.JWT_AUTH.CLAIM_USER_SLUG);
		
		return apiFilterPermissionRepository.findAll(getEntityClass(), getPermissionEntityName(), requestFilter, slugUser);
	}
	
	public ENTITY save(ENTITY entity, String authorization) throws ApiException {
		String token = TokenUtils.getTokenFromAuthorizationHeader(authorization);
		String userSlug = tokenAuthenticationService.getTokenClaim(token, Constants.JWT_AUTH.CLAIM_USER_SLUG);
		User user = null;
		
		try {
			user = userService.findBySlug(userSlug);
			
		} catch (NotFoundApiException e) {
			throw new BadRequestApiException(String.format(Constants.MSG_ERROR.USER_NOT_FOUND_SLUG_ERROR, userSlug));
		}
		
		ENTITY savedEntity = save(entity);
		
		Permission entityPermission = Permission.builder()
			.user(user)
			.role(PermissionRole.ADMIN)
			.build();
		
		try {
			ReflectionUtils.setEntityFieldByClass(entityPermission, savedEntity, getEntityClass());
			
		} catch (Exception e) {
			throw new BadRequestApiException(String.format(
					Constants.MSG_ERROR.INVALID_ENTITY_PERMISSION_ERROR, savedEntity.getClass().getSimpleName()), e);
		}
		
		permissionService.save(entityPermission);
		
		return savedEntity;
	}
	
	public Boolean allowPermissionWriteAccess(String authorization, String slug) {
		String token = TokenUtils.getTokenFromAuthorizationHeader(authorization);
		String userSlug = tokenAuthenticationService.getTokenClaim(token, Constants.JWT_AUTH.CLAIM_USER_SLUG);
		User user = null;
		
		try {
			user = userService.findBySlug(userSlug);
			
		} catch (NotFoundApiException e) {
			return Boolean.FALSE;
		}
		
		if (user == null) {
			return Boolean.FALSE;
		}
		
		if (Role.ADMIN.equals(user.getRole())) {
			return Boolean.TRUE;
		}
		
		Permission userPermission = permissionService.findOneByUserAndEntityNameAndEntitySlug(
				user, getPermissionEntityName(), slug);
		
		if (userPermission == null || PermissionRole.READ.equals(userPermission.getRole())) {
			return Boolean.FALSE;
		}
		
		return Boolean.TRUE;
	}

	public Boolean allowPermissionReadAccess(String authorization, String slug) {
		String token = TokenUtils.getTokenFromAuthorizationHeader(authorization);
		String userSlug = tokenAuthenticationService.getTokenClaim(token, Constants.JWT_AUTH.CLAIM_USER_SLUG);
		User user = null;
		
		try {
			user = userService.findBySlug(userSlug);
			
		} catch (NotFoundApiException e) {
			return Boolean.FALSE;
		}
		
		if (user == null) {
			return Boolean.FALSE;
		}
		
		if (Role.ADMIN.equals(user.getRole())) {
			return Boolean.TRUE;
		}
		
		Permission userPermission = permissionService.findOneByUserAndEntityNameAndEntitySlug(
				user, getPermissionEntityName(), slug);
		
		if (userPermission == null) {
			return Boolean.FALSE;
		}
		
		return Boolean.TRUE;
	}
	
	protected abstract String getPermissionEntityName();
	
}