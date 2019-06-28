package com.uff.phenomanager.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.uff.phenomanager.Constants;
import com.uff.phenomanager.domain.ComputationalModel;
import com.uff.phenomanager.domain.Experiment;
import com.uff.phenomanager.domain.Hypothesis;
import com.uff.phenomanager.domain.Permission;
import com.uff.phenomanager.domain.Phenomenon;
import com.uff.phenomanager.domain.Project;
import com.uff.phenomanager.domain.User;
import com.uff.phenomanager.exception.ApiException;
import com.uff.phenomanager.exception.BadRequestApiException;
import com.uff.phenomanager.exception.NotFoundApiException;
import com.uff.phenomanager.repository.PermissionRepository;
import com.uff.phenomanager.service.core.ApiRestService;

@Service
public class PermissionService extends ApiRestService<Permission, PermissionRepository> {
	
	@Autowired
	private PermissionRepository permissionRepository;
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private TeamService teamService;
	
	@Autowired
	private ProjectService projectService;
	
	@Autowired
	private PhenomenonService phenomenonService;
	
	@Autowired
	private HypothesisService hypothesisService;
	
	@Autowired
	private ExperimentService experimentService;

	@Autowired
	private ComputationalModelService computationalModelService;
	
	@Override
	protected PermissionRepository getRepository() {
		return permissionRepository;
	}
	
	@Override
	protected Class<Permission> getEntityClass() {
		return Permission.class;
	}
	
	@Override
	public Permission save(Permission permission) throws ApiException {
		fillUserTeamPermission(permission);
		fillEntityPermission(permission);

		Permission permissionDatabase = getDuplicateEntityPermission(permission);
		if (permissionDatabase != null) {
			throw new BadRequestApiException(
					String.format(Constants.MSG_ERROR.DUPLICATE_USER_PERMISSION_ERROR, 
							permission.getUser().getSlug()));
		}
		
		return super.save(permission);
   }

	private void fillUserTeamPermission(Permission permission) throws BadRequestApiException {
		if (permission.getUser() != null) {
			try {
				permission.setUser(userService.findBySlug(permission.getUser().getSlug()));

			} catch (NotFoundApiException e) {
				throw new BadRequestApiException(
						String.format(Constants.MSG_ERROR.PERMISSION_USER_NOT_FOUND_ERROR, 
								permission.getUser().getSlug()), e);
			}
		}
		
		if (permission.getTeam() != null) {
			try {
				permission.setTeam(teamService.findBySlug(permission.getTeam().getSlug()));
				
			} catch (NotFoundApiException e) {
				throw new BadRequestApiException(
						String.format(Constants.MSG_ERROR.PERMISSION_TEAM_NOT_FOUND_ERROR, 
								permission.getTeam().getSlug()), e);
			}
		}
	}

	private void fillEntityPermission(Permission permission) throws BadRequestApiException {
		if (permission.getProject() != null) {
			try {
				permission.setProject(projectService.findBySlug(permission.getProject().getSlug()));
				
			} catch (NotFoundApiException e) {
				throw new BadRequestApiException(
						String.format(Constants.MSG_ERROR.ENTITY_PERMISSION_NOT_FOUND_ERROR, 
							Project.class.getSimpleName(),	permission.getProject().getSlug()), e);
			}
			
		} else if (permission.getPhenomenon() != null) {
			try {
				permission.setPhenomenon(phenomenonService.findBySlug(permission.getPhenomenon().getSlug()));
				
			} catch (NotFoundApiException e) {
				throw new BadRequestApiException(
						String.format(Constants.MSG_ERROR.ENTITY_PERMISSION_NOT_FOUND_ERROR, 
							Phenomenon.class.getSimpleName(),	permission.getPhenomenon().getSlug()), e);
			}
			
		} else if (permission.getHypothesis() != null) {
			try {
				permission.setHypothesis(hypothesisService.findBySlug(permission.getHypothesis().getSlug()));
				
			} catch (NotFoundApiException e) {
				throw new BadRequestApiException(
						String.format(Constants.MSG_ERROR.ENTITY_PERMISSION_NOT_FOUND_ERROR, 
							Hypothesis.class.getSimpleName(),	permission.getHypothesis().getSlug()), e);
			}
			
		} else if (permission.getExperiment() != null) {
			try {
				permission.setExperiment(experimentService.findBySlug(permission.getExperiment().getSlug()));
				
			} catch (NotFoundApiException e) {
				throw new BadRequestApiException(
						String.format(Constants.MSG_ERROR.ENTITY_PERMISSION_NOT_FOUND_ERROR, 
							Experiment.class.getSimpleName(),	permission.getExperiment().getSlug()), e);
			}
			
		} else if (permission.getComputationalModel() != null) {
			try {
				permission.setComputationalModel(computationalModelService.findBySlug(
						permission.getComputationalModel().getSlug()));
				
			} catch (NotFoundApiException e) {
				throw new BadRequestApiException(
						String.format(Constants.MSG_ERROR.ENTITY_PERMISSION_NOT_FOUND_ERROR, 
							ComputationalModel.class.getSimpleName(),	permission.getComputationalModel().getSlug()), e);
			}
		}
	}

	private Permission getDuplicateEntityPermission(Permission permission) {
		Permission permissionDatabase = null;
		
		if (permission.getProject() != null) {
			if (permission.getUser() != null) {
				permissionDatabase = permissionRepository.findByUserAndProject(
						permission.getUser(), permission.getProject());
			
			} else if (permission.getTeam() != null) {
				permissionDatabase = permissionRepository.findByTeamAndProject(
						permission.getTeam(), permission.getProject());
			}
			
		} else if (permission.getPhenomenon() != null) {
			if (permission.getUser() != null) {
				permissionDatabase = permissionRepository.findByUserAndPhenomenon(
						permission.getUser(), permission.getPhenomenon());
			
			} else if (permission.getTeam() != null) {
				permissionDatabase = permissionRepository.findByTeamAndPhenomenon(
						permission.getTeam(), permission.getPhenomenon());
			}
			
		} else if (permission.getHypothesis() != null) {
			if (permission.getUser() != null) {
				permissionDatabase = permissionRepository.findByUserAndHypothesis(
						permission.getUser(), permission.getHypothesis());
			
			} else if (permission.getTeam() != null) {
				permissionDatabase = permissionRepository.findByTeamAndHypothesis(
						permission.getTeam(), permission.getHypothesis());
			}
			
		} else if (permission.getExperiment() != null) {
			if (permission.getUser() != null) {
				permissionDatabase = permissionRepository.findByUserAndExperiment(
						permission.getUser(), permission.getExperiment());
			
			} else if (permission.getTeam() != null) {
				permissionDatabase = permissionRepository.findByTeamAndExperiment(
						permission.getTeam(), permission.getExperiment());
			}
			
		} else if (permission.getComputationalModel() != null) {
			if (permission.getUser() != null) {
				permissionDatabase = permissionRepository.findByUserAndComputationalModel(
						permission.getUser(), permission.getComputationalModel());
			
			} else if (permission.getTeam() != null) {
				permissionDatabase = permissionRepository.findByTeamAndComputationalModel(
						permission.getTeam(), permission.getComputationalModel());
			}
		}
		
		return permissionDatabase;
	}
	
	public Permission findOneByUserAndEntityNameAndEntitySlug(User user, String entityName, String entitySlug) {
		return permissionRepository.findOneByUserAndEntityNameAndEntitySlug(user, entityName, entitySlug);
	}
	
	public Integer deleteByProject(Project project) {
		return permissionRepository.deleteByProject(project);
	}
	
	public Integer deleteByPhenomenon(Phenomenon phenomenon) {
		return permissionRepository.deleteByPhenomenon(phenomenon);
	}
	
	public Integer deleteByHypothesis(Hypothesis hypothesis) {
		return permissionRepository.deleteByHypothesis(hypothesis);
	}
	
	public Integer deleteByExperiment(Experiment experiment) {
		return permissionRepository.deleteByExperiment(experiment);
	}
	
	public Integer deleteByComputationalModel(ComputationalModel computationalModel) {
		return permissionRepository.deleteByComputationalModel(computationalModel);
	}
	
}