package com.uff.phenomanager.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.uff.phenomanager.Constants.CONTROLLER;
import com.uff.phenomanager.Constants.JWT_AUTH;
import com.uff.phenomanager.domain.User;
import com.uff.phenomanager.domain.core.ApiResponse;
import com.uff.phenomanager.domain.core.filter.RequestFilter;
import com.uff.phenomanager.exception.ApiException;
import com.uff.phenomanager.exception.NotFoundApiException;
import com.uff.phenomanager.service.UserService;

@RestController
@RequestMapping(CONTROLLER.USER.PATH)
public class UserController {
	
	private static final Logger log = LoggerFactory.getLogger(UserController.class);
	
	@Autowired
	private UserService userService;
	
	@GetMapping(value = CONTROLLER.SLUG_PATH, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<User> getBySlug(@PathVariable(CONTROLLER.SLUG) String slug) throws NotFoundApiException {
		log.info("Processing finOne by slug: [{}]", slug);
		return new ResponseEntity<>(userService.findBySlug(slug), HttpStatus.OK);
	}

    @GetMapping(produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<ApiResponse<User>> getAll(
    		@ModelAttribute("RequestFilter") RequestFilter requestFilter) throws ApiException {
    	log.info("Finding Entity by requestFilter=[{}]", requestFilter);
		return new ResponseEntity<>(userService.findAll(requestFilter), HttpStatus.OK);
    }
	
	@PostMapping(consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	@PreAuthorize("@userService.allowAdminAccess(#authorization)")
    public ResponseEntity<User> insert(@RequestHeader(JWT_AUTH.AUTHORIZATION) String authorization, 
    		@RequestBody User entity) throws ApiException {
    	
		log.info("Processing insert of data: [{}]", entity);
		return new ResponseEntity<>(userService.save(entity), HttpStatus.OK);
    }
    
    @PutMapping(value = CONTROLLER.SLUG_PATH, 
			consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("@userService.allowAdminAccess(#authorization)")
    public ResponseEntity<User> update(@RequestHeader(JWT_AUTH.AUTHORIZATION) String authorization, 
    		@PathVariable(CONTROLLER.SLUG) String slug, @RequestBody User entity) throws ApiException {
    	
    	log.info("Processing update of entity of slug: [{}]", slug);
		return new ResponseEntity<>(userService.update(entity), HttpStatus.OK);
    }
    
    @DeleteMapping(value = CONTROLLER.SLUG_PATH, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("@userService.allowAdminAccess(#authorization)")
    public ResponseEntity<Object> delete(@RequestHeader(JWT_AUTH.AUTHORIZATION) String authorization, 
    		@PathVariable(CONTROLLER.SLUG) String slug) throws NotFoundApiException {
    	
    	log.info("Processing delete of entity of slug: [{}]", slug);
    	userService.delete(slug);
		return new ResponseEntity<>(HttpStatus.OK);
    }
    
    @PostMapping(value = CONTROLLER.USER.SYNC_NAME, 
			consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<Object> sync(@RequestHeader(JWT_AUTH.AUTHORIZATION) String authorization,
    		@PathVariable(CONTROLLER.SLUG) String slug) throws ApiException {
		log.info("Processing sync in SciManager of user of slug: [{}]", slug);
		userService.sync(slug, authorization);
    	return new ResponseEntity<>(HttpStatus.OK);
    }
	
}