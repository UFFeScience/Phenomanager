package com.uff.phenomanager.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.uff.phenomanager.Constants;
import com.uff.phenomanager.domain.Experiment;
import com.uff.phenomanager.domain.Phase;
import com.uff.phenomanager.exception.ApiException;
import com.uff.phenomanager.exception.BadRequestApiException;
import com.uff.phenomanager.exception.NotFoundApiException;
import com.uff.phenomanager.repository.PhaseRepository;
import com.uff.phenomanager.service.core.ApiPermissionRestService;
import com.uff.phenomanager.util.KeyUtils;

@Service
public class PhaseService extends ApiPermissionRestService<Phase, PhaseRepository> {
	
	@Autowired
	private PhaseRepository phaseRepository;
	
	@Autowired
	private ExperimentService experimentService;
	
	@Override
	protected PhaseRepository getRepository() {
		return phaseRepository;
	}
	
	@Override
	protected Class<Phase> getEntityClass() {
		return Phase.class;
	}
	
	@Override
	protected String getPermissionEntityName() {
		return StringUtils.uncapitalize(Experiment.class.getSimpleName());
	}

	public Phase save(Phase phase, String experimentSlug) throws ApiException {
		Experiment parentExperiment = null;
		
		if (phase.getSlug() == null || "".equals(phase.getSlug())) {
			phase.setSlug(KeyUtils.generate());
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
		
		phase.setExperiment(parentExperiment);
		
		return super.save(phase);
   }
	
	public Phase update(Phase phase, String experimentSlug) throws ApiException {
		Phase phaseDatabase = findBySlug(phase.getSlug());
		phase.setId(phaseDatabase.getId());
		
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
		
		phase.setExperiment(parentExperiment);		
		
		return super.update(phase);
	}
	
	public List<Phase> findAllByExperiment(Experiment experiment) {
		return phaseRepository.findAllByExperiment(experiment);
	}

	public Integer deleteByExperiment(Experiment experiment) {
		return phaseRepository.deleteByExperiment(experiment);
	}
	
}