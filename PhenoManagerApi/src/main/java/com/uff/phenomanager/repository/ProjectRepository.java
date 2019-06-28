package com.uff.phenomanager.repository;

import org.springframework.stereotype.Repository;

import com.uff.phenomanager.domain.Project;
import com.uff.phenomanager.repository.core.BaseRepository;

@Repository
public interface ProjectRepository extends BaseRepository<Project> {}