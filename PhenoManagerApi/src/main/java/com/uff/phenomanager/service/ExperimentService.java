package com.uff.phenomanager.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.uff.phenomanager.Constants;
import com.uff.phenomanager.domain.ComputationalModel;
import com.uff.phenomanager.domain.Experiment;
import com.uff.phenomanager.domain.Hypothesis;
import com.uff.phenomanager.domain.Project;
import com.uff.phenomanager.exception.ApiException;
import com.uff.phenomanager.exception.BadRequestApiException;
import com.uff.phenomanager.exception.NotFoundApiException;
import com.uff.phenomanager.repository.ExperimentRepository;
import com.uff.phenomanager.service.core.ApiPermissionRestService;
import com.uff.phenomanager.util.KeyUtils;

@Service
public class ExperimentService extends ApiPermissionRestService<Experiment, ExperimentRepository> {
	
	@Autowired
	private ExperimentRepository experimentRepository;
	
	@Autowired
	private HypothesisService hypothesisService;
	
	@Autowired
	private ComputationalModelService computationalModelService;
	
	@Autowired
	private ConceptualParamService conceptualParamService;
	
	@Autowired
	private ValidationItemService validationItemService;
	
	@Autowired
	private PhaseService phaseService;
	
	@Override
	protected ExperimentRepository getRepository() {
		return experimentRepository;
	}
	
	@Override
	protected Class<Experiment> getEntityClass() {
		return Experiment.class;
	}
	
	@Override
	protected String getPermissionEntityName() {
		return StringUtils.uncapitalize(getEntityClass().getSimpleName());
	}
	
	@Override
	public Experiment save(Experiment experiment, String authorization) throws ApiException {
		Hypothesis parentEntity = null;
		
		if (experiment.getSlug() == null || "".equals(experiment.getSlug())) {
			experiment.setSlug(KeyUtils.generate());
		}
		
		if (experiment.getHypothesis() == null || experiment.getHypothesis().getSlug() == null ||
				"".equals(experiment.getHypothesis().getSlug())) {
			
			throw new BadRequestApiException(String.format(Constants.MSG_ERROR.PARENT_ENTITY_NULL_ERROR, Hypothesis.class.getName()));
		}
		
		try {
			parentEntity = hypothesisService.findBySlug(experiment.getHypothesis().getSlug());
		} catch (NotFoundApiException e) {
			throw new BadRequestApiException(
					String.format(Constants.MSG_ERROR.PARENT_ENTITY_NOT_FOUND_ERROR, Hypothesis.class.getName(),
							experiment.getHypothesis().getSlug()), e);
		}
		
		experiment.setHypothesis(parentEntity);
		
		return super.save(experiment, authorization);
	}
	
	@Override
	public Experiment update(Experiment experiment) throws ApiException {
		Experiment experimentDatabase = findBySlug(experiment.getSlug());
		experiment.setId(experimentDatabase.getId());
		
		Hypothesis parentEntity = null;
		
		if (experiment.getHypothesis() == null || experiment.getHypothesis().getSlug() == null ||
				"".equals(experiment.getHypothesis().getSlug())) {
			
			throw new BadRequestApiException(String.format(Constants.MSG_ERROR.PARENT_ENTITY_NULL_ERROR, Hypothesis.class.getName()));
		}
		
		try {
			parentEntity = hypothesisService.findBySlug(experiment.getHypothesis().getSlug());
		} catch (NotFoundApiException e) {
			throw new BadRequestApiException(
					String.format(Constants.MSG_ERROR.PARENT_ENTITY_NOT_FOUND_ERROR, Hypothesis.class.getName(),
							experiment.getHypothesis().getSlug()), e);
		}
		
		experiment.setHypothesis(parentEntity);

		return super.update(experiment);
	}
	
	public List<Experiment> findAllByHypothesisPhenomenonProject(Project project) {
		return experimentRepository.findAllByHypothesisPhenomenonProject(project);
	}
	
	@Override
	public Integer delete(String slug) throws ApiException {
		Experiment experiment = findBySlug(slug);
		
		List<ComputationalModel> computationalModels = computationalModelService.findAllByExperiment(experiment);
		if (computationalModels != null && !computationalModels.isEmpty()) {
			for (ComputationalModel computationalModel : computationalModels) {
				computationalModelService.delete(computationalModel.getSlug());
			}
		}
		
		conceptualParamService.deleteByExperiment(experiment);
		validationItemService.deleteByExperiment(experiment);
		phaseService.deleteByExperiment(experiment);
		permissionService.deleteByExperiment(experiment);
		
		return super.delete(slug);
	}

	public List<Experiment> findAllByHypothesis(Hypothesis hypothesis) {
		return experimentRepository.findAllByHypothesis(hypothesis);
	}
	
}