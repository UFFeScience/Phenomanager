package com.uff.phenomanager.controller.core;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.uff.phenomanager.Constants.CONTROLLER;
import com.uff.phenomanager.Constants.JWT_AUTH;
import com.uff.phenomanager.config.security.WithoutSecurity;
import com.uff.phenomanager.exception.NotFoundApiException;
import com.uff.phenomanager.service.core.AuthenticationService;

@RestController
@RequestMapping(CONTROLLER.LOGIN.PATH)
public class AuthenticationController {
	
	private static final Logger log = LoggerFactory.getLogger(AuthenticationController.class);
	
	@Autowired
	private AuthenticationService authenticationService;
	
	@WithoutSecurity
	@PostMapping(consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<Map<String, String>> authenticate(@RequestBody  Map<String, String> credentials) throws NotFoundApiException {
		log.info("Processing login for user credentials: [{}]", credentials != null ? credentials.get(JWT_AUTH.CLAIM_EMAIL) : null);
		return new ResponseEntity<>(authenticationService.attemptAuthentication(credentials), HttpStatus.OK);
	}
	
}