package com.uff.phenomanager.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.uff.phenomanager.Constants;
import com.uff.phenomanager.domain.Experiment;
import com.uff.phenomanager.domain.Hypothesis;
import com.uff.phenomanager.domain.Phenomenon;
import com.uff.phenomanager.exception.ApiException;
import com.uff.phenomanager.exception.BadRequestApiException;
import com.uff.phenomanager.exception.NotFoundApiException;
import com.uff.phenomanager.repository.HypothesisRepository;
import com.uff.phenomanager.service.core.ApiPermissionRestService;
import com.uff.phenomanager.util.KeyUtils;

@Service
public class HypothesisService extends ApiPermissionRestService<Hypothesis, HypothesisRepository> {
	
	@Autowired
	private HypothesisRepository hypothesisRepository;
	
	@Autowired
	private PhenomenonService phenomenonService;
	
	@Autowired
	private ExperimentService experimentService;
	
	@Override
	protected HypothesisRepository getRepository() {
		return hypothesisRepository;
	}
	
	@Override
	protected Class<Hypothesis> getEntityClass() {
		return Hypothesis.class;
	}
	
	@Override
	protected String getPermissionEntityName() {
		return StringUtils.uncapitalize(getEntityClass().getSimpleName());
	}
	
	@Override
	public Hypothesis save(Hypothesis hypothesis, String authorization) throws ApiException {
		Phenomenon parentEntity = null;
		
		if (hypothesis.getSlug() == null || "".equals(hypothesis.getSlug())) {
			hypothesis.setSlug(KeyUtils.generate());
		}
		
		if (hypothesis.getPhenomenon() == null || hypothesis.getPhenomenon().getSlug() == null ||
				"".equals(hypothesis.getPhenomenon().getSlug())) {
			
			throw new BadRequestApiException(String.format(Constants.MSG_ERROR.PARENT_ENTITY_NULL_ERROR, Phenomenon.class.getName()));
		}
		
		try {
			parentEntity = phenomenonService.findBySlug(hypothesis.getPhenomenon().getSlug());
		} catch (NotFoundApiException e) {
			throw new BadRequestApiException(
					String.format(Constants.MSG_ERROR.PARENT_ENTITY_NOT_FOUND_ERROR, Phenomenon.class.getName(),
							hypothesis.getPhenomenon().getSlug()), e);
		}
		
		hypothesis.setPhenomenon(parentEntity);
		
		if (hypothesis.getParentHypothesis() != null) {
			try {
				Hypothesis parentHypothesisDatabase = findBySlug(hypothesis.getParentHypothesis().getSlug());
				hypothesis.setParentHypothesis(parentHypothesisDatabase);
				
			} catch (NotFoundApiException e) {
				throw new BadRequestApiException(
						String.format(Constants.MSG_ERROR.PARENT_ENTITY_HYPOTHESIS_NOT_FOUND_ERROR, 
								hypothesis.getParentHypothesis().getSlug(), hypothesis.getPhenomenon().getSlug()), e);
			}
		}
		
		return super.save(hypothesis, authorization);
	}
	
	@Override
	public Hypothesis update(Hypothesis hypothesis) throws ApiException {
		Hypothesis hypothesisDatabase = findBySlug(hypothesis.getSlug());
		hypothesis.setId(hypothesisDatabase.getId());
		
		Phenomenon parentEntity = null;
		
		if (hypothesis.getPhenomenon() == null || hypothesis.getPhenomenon().getSlug() == null ||
				"".equals(hypothesis.getPhenomenon().getSlug())) {
			
			throw new BadRequestApiException(String.format(Constants.MSG_ERROR.PARENT_ENTITY_NULL_ERROR, Phenomenon.class.getName()));
		}
		
		try {
			parentEntity = phenomenonService.findBySlug(hypothesis.getPhenomenon().getSlug());
		} catch (NotFoundApiException e) {
			throw new BadRequestApiException(
					String.format(Constants.MSG_ERROR.PARENT_ENTITY_NOT_FOUND_ERROR, Phenomenon.class.getName(),
							hypothesis.getPhenomenon().getSlug()), e);
		}
		
		hypothesis.setPhenomenon(parentEntity);
		
		if (hypothesis.getParentHypothesis() != null) {
			try {
				Hypothesis parentHypothesisDatabase = findBySlug(hypothesis.getParentHypothesis().getSlug());
				hypothesis.setParentHypothesis(parentHypothesisDatabase);
				
			} catch (NotFoundApiException e) {
				throw new BadRequestApiException(
						String.format(Constants.MSG_ERROR.PARENT_ENTITY_HYPOTHESIS_NOT_FOUND_ERROR, 
								hypothesis.getParentHypothesis().getSlug(), hypothesis.getPhenomenon().getSlug()), e);
			}
		}
		
		return super.update(hypothesis);
	}
	
	@Override
	public Integer delete(String slug) throws NotFoundApiException {
		Hypothesis hypothesis = findBySlug(slug);
		
		List<Experiment> experiments = experimentService.findAllByHypothesis(hypothesis);
		if (experiments != null && !experiments.isEmpty()) {
			for (Experiment experiment : experiments) {
				experimentService.delete(experiment.getSlug());
			}
		}
		
		hypothesisRepository.updateParentToNull(hypothesis.getSlug());
		permissionService.deleteByHypothesis(hypothesis);
		
		return super.delete(slug);
	}
	
	List<Hypothesis> findAllByPhenomenon(Phenomenon phenomenon) {
		return hypothesisRepository.findAllByPhenomenon(phenomenon);
	}
	
}