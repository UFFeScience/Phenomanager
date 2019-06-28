package com.uff.phenomanager.service.api;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.uff.phenomanager.Constants;
import com.uff.phenomanager.Constants.JWT_AUTH;
import com.uff.phenomanager.domain.ComputationalModel;
import com.uff.phenomanager.domain.Experiment;
import com.uff.phenomanager.domain.Permission;
import com.uff.phenomanager.domain.Project;
import com.uff.phenomanager.domain.api.scimanager.Phase;
import com.uff.phenomanager.domain.api.scimanager.ProfileImage;
import com.uff.phenomanager.domain.api.scimanager.ScientificExperiment;
import com.uff.phenomanager.domain.api.scimanager.ScientificProject;
import com.uff.phenomanager.domain.api.scimanager.User;
import com.uff.phenomanager.domain.api.scimanager.UserGroup;
import com.uff.phenomanager.domain.api.scimanager.Workflow;
import com.uff.phenomanager.exception.NotFoundApiException;
import com.uff.phenomanager.service.ComputationalModelService;
import com.uff.phenomanager.service.ExperimentService;
import com.uff.phenomanager.service.PhaseService;
import com.uff.phenomanager.service.api.google.GoogleDriveService;
import com.uff.phenomanager.util.KeyUtils;

@Lazy
@Service
public class SciManagerService {
	
	private static final Logger log = LoggerFactory.getLogger(SciManagerService.class);
	
	@Autowired
	private ExperimentService experimentService;
	
	@Autowired
	private ComputationalModelService computationalModelService;
	
	@Autowired
	private PhaseService phaseService;
	
	@Lazy
	@Autowired
	private GoogleDriveService googleDriveService;
	
	@Value(Constants.API_CLIENT.SCI_MANAGER.BASE_DOMAIN_URL)
	private String baseDomainUrl;
	
	@Async
	public void syncProject(Project project, String authorization) {
		if (project == null) {
			log.info("Can not sync null project");
			return;
		}
		
		log.info("Starting sync of Project entity of slug [{}] in SciManager Api", project.getSlug());
		
		ScientificProject scientificProjectBody = buildScientificProjectBody(project);
		
		HttpHeaders headers = new HttpHeaders();
		headers.add(JWT_AUTH.AUTHORIZATION, authorization);
		
		RestTemplate restTemplate = new RestTemplate();
		HttpEntity<Object> request = new HttpEntity<>(scientificProjectBody);
		
		ResponseEntity<Object> response = restTemplate
		  .exchange(baseDomainUrl + Constants.API_CLIENT.SCI_MANAGER.SCIENTIFIC_PROJECT_SYNC_PATH, 
				  HttpMethod.POST, request, Object.class);
		
		log.info("Sync realized in SciManager with status [{}] and body [{}]", 
				response.getStatusCodeValue(), response.getBody());
	}

	private ScientificProject buildScientificProjectBody(Project project) {
		List<ScientificExperiment> scientificExperiments = new ArrayList<>();
		List<Phase> phases = new ArrayList<>();

		ScientificProject scientificProject = ScientificProject.builder()
				.projectName(project.getName())
				.slug(project.getSlug())
				.build();
		
		List<Experiment> experiments = experimentService.findAllByHypothesisPhenomenonProject(project);
		
		if (experiments != null && !experiments.isEmpty()) {
			
			for (Experiment experiment : experiments) {
				scientificExperiments.add(buildScientificExperimentBody(experiment));
				
				List<com.uff.phenomanager.domain.Phase> experimentPhases = phaseService.findAllByExperiment(experiment);
				
				if (phases != null && !phases.isEmpty()) {
					
					for (com.uff.phenomanager.domain.Phase experimentPhase : experimentPhases) {
						phases.add(Phase.builder()
								.phaseName(experimentPhase.getName())
								.slug(experimentPhase.getSlug())
								.build());
					}
				}
			}
		}
		
		scientificProject.setPhases(phases);
		
		return scientificProject;
	}

	private ScientificExperiment buildScientificExperimentBody(Experiment experiment) {
		List<Workflow> workflows = new ArrayList<>();
		
		ScientificExperiment scientificExperiment = ScientificExperiment.builder()
				.experimentName(experiment.getName())
				.slug(experiment.getSlug())
				.build();
		
		List<ComputationalModel> computationalModels = computationalModelService
				.findAllByExperiment(experiment);
		
		if (computationalModels != null && !computationalModels.isEmpty()) {
			
			for (ComputationalModel computationalModel : computationalModels) {
				workflows.add(Workflow.builder()
						.currentVersion(computationalModel.getCurrentVersion())
						.slug(computationalModel.getSlug())
						.workflowName(computationalModel.getName())
						.responsibleGroup(buildUserGroup(computationalModel))
						.build());
			}
		}
		
		scientificExperiment.setWorkflows(workflows);
		
		return scientificExperiment;
	}

	private UserGroup buildUserGroup(ComputationalModel computationalModel) {
		if (computationalModel == null || computationalModel.getPermissions() == null 
				|| computationalModel.getPermissions().isEmpty()) {
			return null;
		}
		
		Boolean hasMixedTeams = Boolean.FALSE;
		UserGroup userGroup = UserGroup.builder().build();
		
		for (Permission permission : computationalModel.getPermissions()) {
			if (permission.getUser() != null) {
				userGroup.addUserToTeam(buildUserBody(permission.getUser()));
				hasMixedTeams = Boolean.TRUE;
			}
			
			if (permission.getTeam() != null) {
				for (com.uff.phenomanager.domain.User teamUser : permission.getTeam().getTeamUsers()) {
					userGroup.addUserToTeam(buildUserBody(teamUser));
				}
				
				userGroup.setGroupName(permission.getTeam().getName());
				userGroup.setSlug(permission.getTeam().getSlug());
			}
		}
		
		if (hasMixedTeams) {
			userGroup.setGroupName(computationalModel.getName());
			userGroup.setSlug(KeyUtils.generate());
		}
		
		return userGroup;
	}
	
	@Async
	public void syncUser(com.uff.phenomanager.domain.User user, String authorization) {
		if (user == null) {
			log.info("Can not sync null project");
			return;
		}
		
		log.info("Starting sync of User entity of slug [{}] in SciManager Api", user.getSlug());
		
		User userBody = buildUserBody(user);
		
		HttpHeaders headers = new HttpHeaders();
		headers.add(JWT_AUTH.AUTHORIZATION, authorization);
		
		RestTemplate restTemplate = new RestTemplate();
		HttpEntity<Object> request = new HttpEntity<>(userBody);
		
		ResponseEntity<Object> response = restTemplate
		  .exchange(baseDomainUrl + Constants.API_CLIENT.SCI_MANAGER.USER_SYNC_PATH, 
				  HttpMethod.POST, request, Object.class);
		
		log.info("Sync realized in SciManager with status [{}] and body [{}]", 
				response.getStatusCodeValue(), response.getBody());
	}
	
	private User buildUserBody(com.uff.phenomanager.domain.User groupUser) {
		User user = User.builder()
				.email(groupUser.getEmail())
				.institution(groupUser.getInstitutionName())
				.password(groupUser.getPassword())
				.slug(groupUser.getSlug())
				.username(groupUser.getName())
				.build();
		
		if (groupUser.getProfileImageFileId() != null && !"".equals(groupUser.getProfileImageFileId())) {
			try {
				user.setProfileImage(ProfileImage.builder()
						.profileImageContent(googleDriveService.getFileBytesContent(groupUser.getProfileImageFileId()))
						.build());
			
			} catch (NotFoundApiException e) {
				log.warn(Constants.MSG_WARN.GOOGLE_FILE_NOT_FOUND);
			}
		}
		
		return user;
	}
	
}