package com.uff.phenomanager.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.uff.phenomanager.domain.Phenomenon;
import com.uff.phenomanager.domain.Project;
import com.uff.phenomanager.exception.ApiException;
import com.uff.phenomanager.exception.NotFoundApiException;
import com.uff.phenomanager.repository.ProjectRepository;
import com.uff.phenomanager.service.api.SciManagerService;
import com.uff.phenomanager.service.core.ApiPermissionRestService;

@Service
public class ProjectService extends ApiPermissionRestService<Project, ProjectRepository> {
	
	@Autowired
	private ProjectRepository projectRepository;
	
	@Autowired
	private PhenomenonService phenomenonService;
	
	@Lazy
	@Autowired
	private SciManagerService sciManagerService;
	
	@Override
	protected ProjectRepository getRepository() {
		return projectRepository;
	}
	
	@Override
	protected Class<Project> getEntityClass() {
		return Project.class;
	}
	
	@Override
	protected String getPermissionEntityName() {
		return StringUtils.uncapitalize(getEntityClass().getSimpleName());
	}

	public void sync(String slug, String authorization) throws NotFoundApiException {
		Project project = findBySlug(slug);
		sciManagerService.syncProject(project, authorization);
	}
	
	@Override
	public Integer delete(String slug) throws ApiException {
		Project project = findBySlug(slug);
		
		List<Phenomenon> phenomenons = phenomenonService.findAllByProject(project);
		if (phenomenons != null && !phenomenons.isEmpty()) {
			for (Phenomenon phenomenon : phenomenons) {
				phenomenonService.delete(phenomenon.getSlug());
			}
		}
		
		permissionService.deleteByProject(project);
		
		return super.delete(slug);
	}
	
}