package com.uff.phenomanager.repository;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.stereotype.Repository;

import com.uff.phenomanager.domain.ComputationalModel;
import com.uff.phenomanager.domain.ConceptualParam;
import com.uff.phenomanager.domain.InstanceParam;
import com.uff.phenomanager.repository.core.BaseRepository;

@Repository
public interface InstanceParamRepository extends BaseRepository<InstanceParam> {
	
	List<InstanceParam> findAllByComputationalModel(ComputationalModel computationalModel);
	
	List<InstanceParam> findAllByConceptualParam(ConceptualParam conceptualParam);

	@Transactional
	Integer deleteByComputationalModel(ComputationalModel computationalModel);
	
	@Transactional
	Integer deleteByConceptualParam(ConceptualParam conceptualParam);
	
}