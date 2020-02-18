package com.uff.model.invoker.repository;

import org.springframework.stereotype.Repository;

import com.uff.model.invoker.domain.User;
import com.uff.model.invoker.repository.core.BaseRepository;

@Repository
public interface UserRepository extends BaseRepository<User> {}