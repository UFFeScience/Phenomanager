package com.uff.phenomanager.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.uff.phenomanager.Constants;
import com.uff.phenomanager.domain.ConceptualParam;
import com.uff.phenomanager.domain.Experiment;
import com.uff.phenomanager.exception.ApiException;
import com.uff.phenomanager.exception.BadRequestApiException;
import com.uff.phenomanager.exception.NotFoundApiException;
import com.uff.phenomanager.repository.ConceptualParamRepository;
import com.uff.phenomanager.service.core.ApiPermissionRestService;
import com.uff.phenomanager.util.KeyUtils;

@Service
public class ConceptualParamService extends ApiPermissionRestService<ConceptualParam, ConceptualParamRepository> {
	
	@Autowired
	private ConceptualParamRepository conceptualParamRepository;
	
	@Autowired
	private ExperimentService experimentService;
	
	@Autowired
	private InstanceParamService instanceParamService;
	
	@Override
	protected ConceptualParamRepository getRepository() {
		return conceptualParamRepository;
	}
	
	@Override
	protected Class<ConceptualParam> getEntityClass() {
		return ConceptualParam.class;
	}
	
	@Override
	protected String getPermissionEntityName() {
		return StringUtils.uncapitalize(Experiment.class.getSimpleName());
	}
	
	public ConceptualParam save(ConceptualParam conceptualParam, String experimentSlug) throws ApiException {
		Experiment parentExperiment = null;
		
		if (conceptualParam.getSlug() == null || "".equals(conceptualParam.getSlug())) {
			conceptualParam.setSlug(KeyUtils.generate());
		}
		
		if (experimentSlug == null || "".equals(experimentSlug)) {
			throw new BadRequestApiException(String.format(Constants.MSG_ERROR.PARENT_ENTITY_NULL_ERROR, Experiment.class.getName()));
		}
		
		try {
			parentExperiment = experimentService.findBySlug(experimentSlug);
		} catch (NotFoundApiException e) {
			throw new BadRequestApiException(
					String.format(Constants.MSG_ERROR.PARENT_ENTITY_NOT_FOUND_ERROR, Experiment.class.getName(), 
							experimentSlug), e);
		}
		
		conceptualParam.setExperiment(parentExperiment);
		
		return super.save(conceptualParam);
	}
	
	public ConceptualParam update(ConceptualParam conceptualParam, String experimentSlug) throws ApiException {
		ConceptualParam conceptualParamDatabase = findBySlug(conceptualParam.getSlug());
		conceptualParam.setId(conceptualParamDatabase.getId());
		
		Experiment parentExperiment = null;
		
		if (experimentSlug == null || "".equals(experimentSlug)) {
			throw new BadRequestApiException(String.format(Constants.MSG_ERROR.PARENT_ENTITY_NULL_ERROR, Experiment.class.getName()));
		}
		
		try {
			parentExperiment = experimentService.findBySlug(experimentSlug);
		} catch (NotFoundApiException e) {
			throw new BadRequestApiException(
					String.format(Constants.MSG_ERROR.PARENT_ENTITY_NOT_FOUND_ERROR, Experiment.class.getName(), 
							experimentSlug), e);
		}
		
		conceptualParam.setExperiment(parentExperiment);		
		
		return super.update(conceptualParam);
	}
	
	@Override
	public Integer delete(String slug) throws NotFoundApiException {
		ConceptualParam conceptualParam = findBySlug(slug);
		
		instanceParamService.deleteByConceptualParam(conceptualParam);
		
		return super.delete(slug);
	}

	public Integer deleteByExperiment(Experiment experiment) {
		List<ConceptualParam> conceptualParams = conceptualParamRepository.findByExperiment(experiment);
		
		if (conceptualParams == null || conceptualParams.isEmpty()) {
			return 0;
		}

		for (ConceptualParam conceptualParam : conceptualParams) {
			instanceParamService.deleteByConceptualParam(conceptualParam);
		}
		
		return conceptualParamRepository.deleteByExperiment(experiment);
	}
	
}