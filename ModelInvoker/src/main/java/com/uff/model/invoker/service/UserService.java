package com.uff.model.invoker.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.uff.model.invoker.domain.User;
import com.uff.model.invoker.repository.UserRepository;

@Service
public class UserService extends ApiRestService<User, UserRepository> {
	
	@Autowired
	private UserRepository userRepository;
	
	@Override
	protected UserRepository getRepository() {
		return userRepository;
	}
	
	@Override
	protected Class<User> getEntityClass() {
		return User.class;
	}
	
}