package com.uff.phenomanager.controller.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.uff.phenomanager.Constants.CONTROLLER;
import com.uff.phenomanager.domain.BaseApiEntity;
import com.uff.phenomanager.domain.core.ApiResponse;
import com.uff.phenomanager.domain.core.filter.RequestFilter;
import com.uff.phenomanager.exception.ApiException;
import com.uff.phenomanager.exception.NotFoundApiException;
import com.uff.phenomanager.service.core.ApiRestService;

@SuppressWarnings({ "rawtypes", "unchecked"} )
public abstract class ApiRestController<ENTITY extends BaseApiEntity, SERVICE extends ApiRestService> {
	
	private static final Logger log = LoggerFactory.getLogger(ApiRestController.class);
	
	public abstract SERVICE getService();
	
	@GetMapping(value = CONTROLLER.SLUG_PATH, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<ENTITY> getBySlug(@PathVariable(CONTROLLER.SLUG) String slug) throws NotFoundApiException {
		log.info("Processing finOne by slug: [{}]", slug);
		return (ResponseEntity<ENTITY>) new ResponseEntity<>(getService().findBySlug(slug), HttpStatus.OK);
	}

    @GetMapping(produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<ApiResponse<ENTITY>> getAll(
    		@ModelAttribute("RequestFilter") RequestFilter requestFilter) throws ApiException {
    	log.info("Finding Entity by requestFilter=[{}]", requestFilter);
		return new ResponseEntity<>(getService().findAll(requestFilter), HttpStatus.OK);
    }
    
    @PostMapping(consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<ENTITY> insert(@RequestBody ENTITY entity) throws ApiException {
    	log.info("Processing insert of data: [{}]", entity);
		return (ResponseEntity<ENTITY>) new ResponseEntity<>(getService().save(entity), HttpStatus.OK);
    }
    
    @PutMapping(value = CONTROLLER.SLUG_PATH, 
			consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<ENTITY> update(@PathVariable(CONTROLLER.SLUG) String slug, 
    		@RequestBody ENTITY entity) throws ApiException {
    	log.info("Processing update of entity of slug: [{}]", slug);
		return (ResponseEntity<ENTITY>) new ResponseEntity<>(getService().update(entity), HttpStatus.OK);
    }
    
    @DeleteMapping(value = CONTROLLER.SLUG_PATH, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<Object> delete(@PathVariable(CONTROLLER.SLUG) String slug) throws NotFoundApiException {
    	log.info("Processing delete of entity of slug: [{}]", slug);
    	getService().delete(slug);
		return new ResponseEntity<>(HttpStatus.OK);
    }
    
}