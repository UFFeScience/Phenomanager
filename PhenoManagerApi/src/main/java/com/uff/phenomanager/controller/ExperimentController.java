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
import com.uff.phenomanager.domain.Experiment;
import com.uff.phenomanager.domain.core.ApiResponse;
import com.uff.phenomanager.domain.core.filter.RequestFilter;
import com.uff.phenomanager.exception.ApiException;
import com.uff.phenomanager.exception.NotFoundApiException;
import com.uff.phenomanager.service.ExperimentService;

@RestController
@RequestMapping(CONTROLLER.EXPERIMENT.PATH)
public class ExperimentController {
	
	private static final Logger log = LoggerFactory.getLogger(ExperimentController.class);
	
	@Autowired
	private ExperimentService experimentService;
	
	@GetMapping(value = CONTROLLER.SLUG_PATH, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	@PreAuthorize("@experimentService.allowPermissionReadAccess(#authorization, #slug)")
    public ResponseEntity<Experiment> getBySlug(@RequestHeader(JWT_AUTH.AUTHORIZATION) String authorization,
    		@PathVariable(CONTROLLER.SLUG) String slug) throws NotFoundApiException {
		log.info("Processing finOne by slug: [{}]", slug);
		return new ResponseEntity<>(experimentService.findBySlug(slug), HttpStatus.OK);
	}

    @GetMapping(produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<ApiResponse<Experiment>> getAll(@RequestHeader(JWT_AUTH.AUTHORIZATION) String authorization,
    		@ModelAttribute("RequestFilter") RequestFilter requestFilter)  throws ApiException{
    	log.info("Finding Entities by requestFilter=[{}]", requestFilter);
		return new ResponseEntity<>(experimentService.findAll(requestFilter, authorization), HttpStatus.OK);
    }
    
    @PostMapping(consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<Experiment> insert(@RequestHeader(JWT_AUTH.AUTHORIZATION) String authorization, 
    		@RequestBody Experiment entity) throws ApiException {
    	
    	log.info("Processing insert of data: [{}]", entity);
		return new ResponseEntity<>(experimentService.save(entity, authorization), HttpStatus.OK);
    }
    
    @PutMapping(value = CONTROLLER.SLUG_PATH, 
			consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("@experimentService.allowPermissionWriteAccess(#authorization, #slug)")
    public ResponseEntity<Experiment> update(@RequestHeader(JWT_AUTH.AUTHORIZATION) String authorization,
    		@PathVariable(CONTROLLER.SLUG) String slug, @RequestBody Experiment entity) throws ApiException {
    	
    	log.info("Processing update of entity of slug: [{}]", slug);
		return new ResponseEntity<>(experimentService.update(entity), HttpStatus.OK);
    }
    
    @DeleteMapping(value = CONTROLLER.SLUG_PATH, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("@experimentService.allowPermissionWriteAccess(#authorization, #slug)")
    public ResponseEntity<Object> delete(@RequestHeader(JWT_AUTH.AUTHORIZATION) String authorization,
    		@PathVariable(CONTROLLER.SLUG) String slug) throws NotFoundApiException {
    	
    	log.info("Processing delete of entity of slug: [{}]", slug);
    	experimentService.delete(slug);
		return new ResponseEntity<>(HttpStatus.OK);
    }
	
}