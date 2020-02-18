package com.uff.model.invoker.repository;

import org.springframework.stereotype.Repository;

import com.uff.model.invoker.domain.Executor;
import com.uff.model.invoker.repository.core.BaseRepository;

@Repository
public interface ExecutorRepository extends BaseRepository<Executor> {}