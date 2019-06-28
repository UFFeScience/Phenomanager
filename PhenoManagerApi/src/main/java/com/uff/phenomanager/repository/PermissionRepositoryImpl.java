package com.uff.phenomanager.repository;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.springframework.beans.factory.annotation.Autowired;

import com.uff.phenomanager.domain.Permission;
import com.uff.phenomanager.domain.User;
import com.uff.phenomanager.domain.Team;

public class PermissionRepositoryImpl implements PermissionRepositoryCustom {
	
	@Autowired
	private EntityManager entityManager;
	
	@Override
	public Permission findOneByUserAndEntityNameAndEntitySlug(User user, String entityName, String entitySlug) {
		List<Predicate> restrictions = new ArrayList<>();
		
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<Permission> query = criteriaBuilder.createQuery(Permission.class);
		Root<Permission> from = query.from(Permission.class);
		restrictions.add(criteriaBuilder.and(criteriaBuilder.equal(from.join(entityName).get("slug"), entitySlug)));
		
		List<Predicate> restrictionsPermission = new ArrayList<>();
		List<Predicate> conjunctionsPermission = new ArrayList<>();
		
		Subquery<Permission> subqueryPermission = query.subquery(Permission.class);
	    Root<?> subqueryPermissionRoot = subqueryPermission.correlate(from);
	    Join<?, Permission> entityPermissions = subqueryPermissionRoot.join("permissions");
	    
	    subqueryPermission
	        .select(entityPermissions)
	        .where(criteriaBuilder.equal(entityPermissions.get("user").get("id"), user.getId()));
	    
	    Subquery<Permission> subqueryTeamUsers = query.subquery(Permission.class);
	    Root<?> subqueryTeamUsersRoot = subqueryTeamUsers.correlate(from);
	    Join<?, Permission> entityTeamUsersPermissions = subqueryTeamUsersRoot.join("permissions");
	    Join<Permission, Team> permissionTeam = entityTeamUsersPermissions.join("team");
	    Join<Team, User> teamUsers = permissionTeam.join("teamUsers");
	    
	    subqueryTeamUsers
	        .select(entityTeamUsersPermissions)
	        .where(criteriaBuilder.equal(teamUsers.get("id"), user.getId()));

	    conjunctionsPermission.add(criteriaBuilder.exists(subqueryPermission));
	    conjunctionsPermission.add(criteriaBuilder.exists(subqueryTeamUsers));
	    restrictionsPermission.add(criteriaBuilder.or(conjunctionsPermission.toArray(new Predicate[]{})));
	    
	    restrictions.add(criteriaBuilder.and(restrictionsPermission.toArray(new Predicate[]{})));
		
		TypedQuery<Permission> typedQuery = entityManager.createQuery(query);
		
		return typedQuery.getSingleResult();
	}

}