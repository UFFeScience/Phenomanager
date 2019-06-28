package com.uff.phenomanager.repository;

import org.springframework.stereotype.Repository;

import com.uff.phenomanager.domain.User;
import com.uff.phenomanager.repository.core.BaseRepository;

@Repository
public interface UserRepository extends BaseRepository<User> {

	User findByEmailAndActive(String email, Boolean active);

}