package com.uff.phenomanager.repository;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.uff.phenomanager.domain.Phenomenon;
import com.uff.phenomanager.domain.Project;
import com.uff.phenomanager.repository.core.BaseRepository;

@Repository
public interface PhenomenonRepository extends BaseRepository<Phenomenon> {

	List<Phenomenon> findAllByProject(Project project);
	
}