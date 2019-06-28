package com.uff.phenomanager.service;

import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.uff.phenomanager.Constants;
import com.uff.phenomanager.domain.Team;
import com.uff.phenomanager.domain.User;
import com.uff.phenomanager.exception.ApiException;
import com.uff.phenomanager.exception.BadRequestApiException;
import com.uff.phenomanager.exception.NotFoundApiException;
import com.uff.phenomanager.repository.TeamRepository;
import com.uff.phenomanager.service.core.ApiRestService;

@Service
public class TeamService extends ApiRestService<Team, TeamRepository> {
	
	@Autowired
	private TeamRepository teamRepository;
	
	@Autowired
	private UserService userService;
	
	@Override
	protected TeamRepository getRepository() {
		return teamRepository;
	}
	
	@Override
	protected Class<Team> getEntityClass() {
		return Team.class;
	}
	
	@Override
	public Team save(Team team) throws ApiException {
		Set<User> teamUsers = new HashSet<>();
		
		for (User user : team.getTeamUsers()) {
			try {
				teamUsers.add(userService.findBySlug(user.getSlug()));

			} catch (NotFoundApiException e) {
				throw new BadRequestApiException(
						String.format(Constants.MSG_ERROR.USER_NOT_FOUND_SLUG_ERROR, user.getSlug()), e);
			}
		}
		
		team.setTeamUsers(teamUsers);
		return super.save(team);
	}
	
	@Override
	public Team update(Team team) throws ApiException {
		Team teamDatabase = findBySlug(team.getSlug());
		team.setId(teamDatabase.getId());
		
		Set<User> teamUsers = new HashSet<>();
		
		for (User user : team.getTeamUsers()) {
			try {
				teamUsers.add(userService.findBySlug(user.getSlug()));

			} catch (NotFoundApiException e) {
				throw new BadRequestApiException(
						String.format(Constants.MSG_ERROR.USER_NOT_FOUND_SLUG_ERROR, user.getSlug()), e);
			}
		}
		
		team.setTeamUsers(teamUsers);
		return super.update(team);
	}
	
}