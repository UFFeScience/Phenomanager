package com.uff.phenomanager.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.uff.phenomanager.Constants;
import com.uff.phenomanager.domain.Hypothesis;
import com.uff.phenomanager.domain.Phenomenon;
import com.uff.phenomanager.domain.Project;
import com.uff.phenomanager.exception.ApiException;
import com.uff.phenomanager.exception.BadRequestApiException;
import com.uff.phenomanager.exception.NotFoundApiException;
import com.uff.phenomanager.repository.PhenomenonRepository;
import com.uff.phenomanager.service.core.ApiPermissionRestService;
import com.uff.phenomanager.util.KeyUtils;

@Service
public class PhenomenonService extends ApiPermissionRestService<Phenomenon, PhenomenonRepository> {
	
	@Autowired
	private PhenomenonRepository phenomenonRepository;
	
	@Autowired
	private ProjectService projectService;
	
	@Autowired
	private HypothesisService hypothesisService;
	
	@Override
	protected PhenomenonRepository getRepository() {
		return phenomenonRepository;
	}
	
	@Override
	protected Class<Phenomenon> getEntityClass() {
		return Phenomenon.class;
	}
	
	@Override
	protected String getPermissionEntityName() {
		return StringUtils.uncapitalize(getEntityClass().getSimpleName());
	}
	
	@Override
	public Phenomenon save(Phenomenon phenomenon, String authorization) throws ApiException {
		Project parentEntity = null;
		
		if (phenomenon.getSlug() == null || "".equals(phenomenon.getSlug())) {
			phenomenon.setSlug(KeyUtils.generate());
		}
		
		if (phenomenon.getProject() == null || phenomenon.getProject().getSlug() == null ||
				"".equals(phenomenon.getProject().getSlug())) {
			
			throw new BadRequestApiException(String.format(Constants.MSG_ERROR.PARENT_ENTITY_NULL_ERROR, Project.class.getName()));
		}
		
		try {
			parentEntity = projectService.findBySlug(phenomenon.getProject().getSlug());
		} catch (NotFoundApiException e) {
			throw new BadRequestApiException(
					String.format(Constants.MSG_ERROR.PARENT_ENTITY_NOT_FOUND_ERROR, Project.class.getName(),
							phenomenon.getProject().getSlug()), e);
		}
		
		phenomenon.setProject(parentEntity);
		
		return super.save(phenomenon, authorization);
	}
	
	@Override
	public Phenomenon update(Phenomenon phenomenon) throws ApiException {
		Phenomenon phenomenonDatabase = findBySlug(phenomenon.getSlug());
		phenomenon.setId(phenomenonDatabase.getId());
		
		Project parentEntity = null;
		
		if (phenomenon.getProject() == null || phenomenon.getProject().getSlug() == null ||
				"".equals(phenomenon.getProject().getSlug())) {
			
			throw new BadRequestApiException(String.format(Constants.MSG_ERROR.PARENT_ENTITY_NULL_ERROR, Project.class.getName()));
		}
		
		try {
			parentEntity = projectService.findBySlug(phenomenon.getProject().getSlug());
		} catch (NotFoundApiException e) {
			throw new BadRequestApiException(
					String.format(Constants.MSG_ERROR.PARENT_ENTITY_NOT_FOUND_ERROR, Project.class.getName(),
							phenomenon.getProject().getSlug()), e);
		}
		
		phenomenon.setProject(parentEntity);

		return super.update(phenomenon);
	}
	
	@Override
	public Integer delete(String slug) throws NotFoundApiException {
		Phenomenon phenomenon = findBySlug(slug);
		
		List<Hypothesis> hypotheses = hypothesisService.findAllByPhenomenon(phenomenon);
		if (hypotheses != null && !hypotheses.isEmpty()) {
			for (Hypothesis hypothesis : hypotheses) {
				hypothesisService.delete(hypothesis.getSlug());
			}
		}
		
		permissionService.deleteByPhenomenon(phenomenon);
		
		return super.delete(slug);
	}

	public List<Phenomenon> findAllByProject(Project project) {
		return phenomenonRepository.findAllByProject(project);
	}
	
}