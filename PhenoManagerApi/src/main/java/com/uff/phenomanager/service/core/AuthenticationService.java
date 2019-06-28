package com.uff.phenomanager.service.core;

import java.util.Collections;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

import com.uff.phenomanager.Constants.CONTROLLER.LOGIN;
import com.uff.phenomanager.Constants.JWT_AUTH;
import com.uff.phenomanager.Constants.MSG_ERROR;
import com.uff.phenomanager.config.security.TokenAuthenticationService;
import com.uff.phenomanager.domain.User;
import com.uff.phenomanager.service.UserService;
import com.uff.phenomanager.util.EncrypterUtils;

@Service
public class AuthenticationService {
	
	@Autowired
	private TokenAuthenticationService tokenAuthenticationService;
	
	@Autowired
	private UserService userService;
	
	public Map<String, String> attemptAuthentication(Map<String, String> credentials) throws AuthenticationException {
		User userAccount = userService.getUserByEmailAndActive(credentials.get(LOGIN.EMAIL_FIELD), Boolean.TRUE);
		
		if (userAccount == null) {
			throw new AuthenticationCredentialsNotFoundException(MSG_ERROR.AUTHENTICATION_ERROR);
		}
		
		if (!EncrypterUtils.matchPassword(credentials.get(LOGIN.PASSWORD_FIELD), userAccount.getPassword())) {
			throw new AuthenticationCredentialsNotFoundException(MSG_ERROR.AUTHENTICATION_ERROR);
		}
		
		return Collections.singletonMap(JWT_AUTH.TOKEN, tokenAuthenticationService.generateToken(userAccount));
	}

}