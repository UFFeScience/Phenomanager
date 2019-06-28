package com.uff.phenomanager.repository;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.uff.phenomanager.domain.Hypothesis;
import com.uff.phenomanager.domain.Phenomenon;
import com.uff.phenomanager.repository.core.BaseRepository;

@Repository
public interface HypothesisRepository extends BaseRepository<Hypothesis> {

	List<Hypothesis> findAllByPhenomenon(Phenomenon phenomenon);
	
	@Transactional
	@Modifying(clearAutomatically = true)
    @Query("UPDATE #{#entityName} as E SET E.parentHypothesis = null WHERE E.slug in "
    	 + "(SELECT E2.slug FROM #{#entityName} E2 where E2.parentHypothesis.slug = :parentHypothesisSlug)")
	Integer updateParentToNull(@Param("parentHypothesisSlug") String parentHypothesisSlug);
	
}