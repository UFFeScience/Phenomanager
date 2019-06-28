package com.uff.phenomanager.repository;

import javax.transaction.Transactional;

import com.uff.phenomanager.domain.ComputationalModel;
import com.uff.phenomanager.domain.Experiment;
import com.uff.phenomanager.domain.Hypothesis;
import com.uff.phenomanager.domain.Permission;
import com.uff.phenomanager.domain.Phenomenon;
import com.uff.phenomanager.domain.Project;
import com.uff.phenomanager.domain.Team;
import com.uff.phenomanager.domain.User;
import com.uff.phenomanager.repository.core.BaseRepository;

public interface PermissionRepository extends BaseRepository<Permission>, PermissionRepositoryCustom {

	@Transactional
	Integer deleteByProject(Project project);

	@Transactional
	Integer deleteByPhenomenon(Phenomenon phenomenon);

	@Transactional
	Integer deleteByHypothesis(Hypothesis hypothesis);

	@Transactional
	Integer deleteByExperiment(Experiment experiment);

	@Transactional
	Integer deleteByComputationalModel(ComputationalModel computationalModel);
	
	Permission findByUserAndProject(User user, Project project);
	
	Permission findByUserAndPhenomenon(User user, Phenomenon phenomenon);
	
	Permission findByUserAndHypothesis(User user, Hypothesis hypothesis);
	
	Permission findByUserAndExperiment(User user, Experiment experiment);
	
	Permission findByUserAndComputationalModel(User user, ComputationalModel computationalModel);
	
	Permission findByTeamAndProject(Team team, Project project);
	
	Permission findByTeamAndPhenomenon(Team team, Phenomenon phenomenon);
	
	Permission findByTeamAndHypothesis(Team team, Hypothesis hypothesis);
	
	Permission findByTeamAndExperiment(Team team, Experiment experiment);
	
	Permission findByTeamAndComputationalModel(Team user, ComputationalModel computationalModel);
	
}