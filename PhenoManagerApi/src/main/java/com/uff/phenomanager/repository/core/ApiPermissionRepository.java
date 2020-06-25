package com.uff.phenomanager.repository.core;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Selection;
import javax.persistence.criteria.Subquery;

import org.postgresql.util.PSQLException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import com.uff.phenomanager.Constants.MSG_ERROR;
import com.uff.phenomanager.domain.Permission;
import com.uff.phenomanager.domain.Team;
import com.uff.phenomanager.domain.User;
import com.uff.phenomanager.domain.core.BaseApiEntity;
import com.uff.phenomanager.domain.core.filter.RequestFilter;
import com.uff.phenomanager.exception.BadRequestApiException;
import com.uff.phenomanager.exception.InternalErrorApiException;
import com.uff.phenomanager.exception.NotFoundApiException;

@Repository
public class ApiPermissionRepository<ENTITY extends BaseApiEntity> extends ApiRepository<ENTITY> {
	
	@Autowired
	private EntityManager entityManager;
	
	public Long countAll(Class<ENTITY> entityClass, String entityPermissionClassName, RequestFilter requestFilter, String slugUser) 
			throws NotFoundApiException, BadRequestApiException, InternalErrorApiException {
		
		requestFilter.processSymbols();
		
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<Long> query = criteriaBuilder.createQuery(Long.class);
		Root<?> root = query.from(entityClass);

		query.select(criteriaBuilder.count(root));
		
		List<Predicate> restrictions = apiQueryBuilder.getRestrictions(entityClass, requestFilter, criteriaBuilder, root); 
		List<Predicate> permissionsRestrictions = getPermissionsRestrictions(entityPermissionClassName, 
				criteriaBuilder, root, query, slugUser);
		
		restrictions.add(criteriaBuilder.and(permissionsRestrictions.toArray(new Predicate[]{})));
		
		query.where(restrictions.toArray(new Predicate[]{}));
		
		try {
		    return entityManager.createQuery(query).getSingleResult();
		
		} catch (NoResultException e) {
			throw new NotFoundApiException(String.format(MSG_ERROR.ENTITIES_NOT_FOUND_ERROR, requestFilter), e);
		
		} catch (PersistenceException e) {
			throw new BadRequestApiException(String.format(MSG_ERROR.BAD_REQUEST_ERROR, requestFilter), e);
		
		} catch (Exception e) {
			throw new InternalErrorApiException(String.format(MSG_ERROR.UNEXPECTED_FETCHING_ERROR, requestFilter), e);
		}
	}
	
	public List<ENTITY> findAll(Class<ENTITY> entityClass, String entityPermissionClassName, RequestFilter requestFilter, String slugUser) 
			throws NotFoundApiException, BadRequestApiException, InternalErrorApiException {
		
		requestFilter.processSymbols();

		if (requestFilter.hasValidAggregateFunction()) {
			return aggregate(entityClass, entityPermissionClassName, requestFilter, slugUser);
		}
		
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<Object> query = criteriaBuilder.createQuery(Object.class);
		Root<?> root = query.from(entityClass);

		List<Selection<? extends Object>> projection = apiQueryBuilder.getProjectionFields(requestFilter, root, entityClass);
		if (!projection.isEmpty() && (projection.size() == 1 || !apiQueryBuilder.containsMultiValuedProjection(projection))) {
			query.multiselect(projection.toArray(new Selection[]{}));
		} else {
			query.select(root);
		}
		
		List<Predicate> restrictions = apiQueryBuilder.getRestrictions(entityClass, requestFilter, criteriaBuilder, root); 
		List<Predicate> permissionsRestrictions = getPermissionsRestrictions(entityPermissionClassName, 
				criteriaBuilder, root, query, slugUser);
		
		restrictions.add(criteriaBuilder.and(permissionsRestrictions.toArray(new Predicate[]{})));
		
		query.where(restrictions.toArray(new Predicate[]{}));
		
		List<Order> orders = apiQueryBuilder.getOrders(requestFilter, criteriaBuilder, root, entityClass);
		if (!orders.isEmpty()) {
			query.orderBy(orders);
		}
		
		try {
		    List<Object> result = (List<Object>) entityManager.createQuery(query)
		    		.setMaxResults(requestFilter.getFetchLimit())
		    		.setFirstResult(requestFilter.getFetchOffset())
		    		.getResultList();
		    
		    return apiResultMapper.mapResultSet(entityClass, result, projection);
		    
		} catch (NoResultException e) {
			throw new NotFoundApiException(String.format(MSG_ERROR.ENTITIES_NOT_FOUND_ERROR, requestFilter), e);
		
		} catch (PSQLException | PersistenceException | IllegalArgumentException e) {
			throw new BadRequestApiException(String.format(MSG_ERROR.BAD_REQUEST_ERROR, requestFilter), e);
			
		} catch (Exception e) {
			throw new InternalErrorApiException(String.format(MSG_ERROR.UNEXPECTED_FETCHING_ERROR, requestFilter), e);
		}
	}
	
	public List<ENTITY> aggregate(Class<ENTITY> entityClass, String entityPermissionClassName, RequestFilter requestFilter, String slugUser) 
			throws NotFoundApiException, BadRequestApiException, InternalErrorApiException {
		
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<Object> query = criteriaBuilder.createQuery(Object.class);
		Root<?> root = query.from(entityClass);

		List<Selection<? extends Object>> aggregationFields = apiQueryBuilder.buildAggregateSelection(root, criteriaBuilder, entityClass, requestFilter);
		
		if (aggregationFields.isEmpty()) {
			throw new BadRequestApiException(String.format(MSG_ERROR.INVALID_AGGREGATION_ERROR, requestFilter));
		}
		
		query.multiselect(aggregationFields.toArray(new Selection[]{}));
		
		List<Predicate> restrictions = apiQueryBuilder.getRestrictions(entityClass, requestFilter, criteriaBuilder, root);
		List<Predicate> permissionsRestrictions = getPermissionsRestrictions(entityPermissionClassName, 
				criteriaBuilder, root, query, slugUser);
		
		restrictions.add(criteriaBuilder.and(permissionsRestrictions.toArray(new Predicate[]{})));
		
		query.where(restrictions.toArray(new Predicate[]{}));
		
		List<Order> orders = apiQueryBuilder.getOrders(requestFilter, criteriaBuilder, root, entityClass);
		if (!orders.isEmpty()) {
			query.orderBy(orders);
		}
		
		List<Selection<? extends Object>> groupBy = apiQueryBuilder.getGroupByFields(requestFilter, root, entityClass);
		query.groupBy(groupBy.toArray(new Expression[]{}));
		
		try {
		    List<Object> result = (List<Object>) entityManager.createQuery(query)
		    		.getResultList();
		    
		    return apiResultMapper.mapResultSet(entityClass, result, aggregationFields);
		    
		} catch (NoResultException e) {
			throw new NotFoundApiException(String.format(MSG_ERROR.ENTITIES_NOT_FOUND_ERROR, requestFilter), e);
		
		} catch (PSQLException | PersistenceException e) {
			throw new BadRequestApiException(String.format(MSG_ERROR.BAD_REQUEST_ERROR, requestFilter), e);
			
		} catch (Exception e) {
			throw new InternalErrorApiException(String.format(MSG_ERROR.UNEXPECTED_FETCHING_ERROR, requestFilter), e);
		}
	}
	
	private List<Predicate> getPermissionsRestrictions(String entityPermissionClassName,
			CriteriaBuilder criteriaBuilder, Root<?> root, CriteriaQuery<?> query, String slugUser) {

		Join<?, Permission> entityPermissions = null;
		Join<?, Permission> entityGroupUsersPermissions = null;
		
		List<Predicate> restrictionsPermission = new ArrayList<>();
		List<Predicate> conjunctionsPermission = new ArrayList<>();
		
		Subquery<Permission> subqueryPermission = query.subquery(Permission.class);
	    Root<?> subqueryPermissionRoot = (Root<?>) subqueryPermission.correlate(root);
	    
	    if (StringUtils.uncapitalize(root.getJavaType().getSimpleName()).equals(entityPermissionClassName)) {
		    entityPermissions = subqueryPermissionRoot.join("permissions");
		} else {
			Join<?, ?> entityParentPermissions = subqueryPermissionRoot.join(entityPermissionClassName);
		    entityPermissions = entityParentPermissions.join("permissions");
		}
	    
	    subqueryPermission
	        .select(entityPermissions)
	        .where(criteriaBuilder.equal(entityPermissions.get("user").get("slug"), slugUser));
	    
	    Subquery<Permission> subqueryGroupUsers = query.subquery(Permission.class);
	    Root<?> subqueryGroupUsersRoot = subqueryGroupUsers.correlate(root);
	    
	    if (StringUtils.uncapitalize(root.getJavaType().getSimpleName()).equals(entityPermissionClassName)) {
	    	entityGroupUsersPermissions = subqueryGroupUsersRoot.join("permissions");
		} else {
			Join<?, ?> entityParentPermissions = subqueryGroupUsersRoot.join(entityPermissionClassName);
		    entityGroupUsersPermissions = entityParentPermissions.join("permissions");
		}
	    
	    Join<Permission, Team> permissionTeam = entityGroupUsersPermissions.join("team");
	    Join<Team, User> groupUsers = permissionTeam.join("teamUsers");
	    
	    subqueryGroupUsers
	        .select(entityGroupUsersPermissions)
	        .where(criteriaBuilder.equal(groupUsers.get("slug"), slugUser));
	
	    conjunctionsPermission.add(criteriaBuilder.exists(subqueryPermission));
	    conjunctionsPermission.add(criteriaBuilder.exists(subqueryGroupUsers));
	    restrictionsPermission.add(criteriaBuilder.or(conjunctionsPermission.toArray(new Predicate[]{})));
	    
		return restrictionsPermission;
	}
	
}