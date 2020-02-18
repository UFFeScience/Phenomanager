package com.uff.phenomanager.repository;

import org.springframework.stereotype.Repository;

import com.uff.phenomanager.domain.Environment;
import com.uff.phenomanager.repository.core.BaseRepository;

@Repository
public interface EnvironmentRepository extends BaseRepository<Environment> {}