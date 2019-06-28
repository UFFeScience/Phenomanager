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
import com.uff.phenomanager.domain.Hypothesis;
import com.uff.phenomanager.domain.core.ApiResponse;
import com.uff.phenomanager.domain.core.filter.RequestFilter;
import com.uff.phenomanager.exception.ApiException;
import com.uff.phenomanager.exception.NotFoundApiException;
import com.uff.phenomanager.service.HypothesisService;

@RestController
@RequestMapping(CONTROLLER.HYPOTHESIS.PATH)
public class HypothesisController {
	
	private static final Logger log = LoggerFactory.getLogger(HypothesisController.class);
	
	@Autowired
	private HypothesisService hypothesisService;
	
	@GetMapping(value = CONTROLLER.SLUG_PATH, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	@PreAuthorize("@hypothesisService.allowPermissionReadAccess(#authorization, #slug)")
    public ResponseEntity<Hypothesis> getBySlug(@RequestHeader(JWT_AUTH.AUTHORIZATION) String authorization,
    		@PathVariable(CONTROLLER.SLUG) String slug) throws NotFoundApiException {
		log.info("Processing finOne by slug: [{}]", slug);
		return new ResponseEntity<>(hypothesisService.findBySlug(slug), HttpStatus.OK);
	}

    @GetMapping(produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<ApiResponse<Hypothesis>> getAll(@RequestHeader(JWT_AUTH.AUTHORIZATION) String authorization,
    		@ModelAttribute("RequestFilter") RequestFilter requestFilter)  throws ApiException{
    	log.info("Finding Entities by requestFilter=[{}]", requestFilter);
		return new ResponseEntity<>(hypothesisService.findAll(requestFilter, authorization), HttpStatus.OK);
    }
    
    @PostMapping(consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<Hypothesis> insert(@RequestHeader(JWT_AUTH.AUTHORIZATION) String authorization, 
    		@RequestBody Hypothesis entity) throws ApiException {
    	
    	log.info("Processing insert of data: [{}]", entity);
		return new ResponseEntity<>(hypothesisService.save(entity, authorization), HttpStatus.OK);
    }
    
    @PutMapping(value = CONTROLLER.SLUG_PATH, 
			consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("@hypothesisService.allowPermissionWriteAccess(#authorization, #slug)")
    public ResponseEntity<Hypothesis> update(@RequestHeader(JWT_AUTH.AUTHORIZATION) String authorization,
    		@PathVariable(CONTROLLER.SLUG) String slug, @RequestBody Hypothesis entity) throws ApiException {
    	
    	log.info("Processing update of entity of slug: [{}]", slug);
		return new ResponseEntity<>(hypothesisService.update(entity), HttpStatus.OK);
    }
    
    @DeleteMapping(value = CONTROLLER.SLUG_PATH, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("@hypothesisService.allowPermissionWriteAccess(#authorization, #slug)")
    public ResponseEntity<Object> delete(@RequestHeader(JWT_AUTH.AUTHORIZATION) String authorization,
    		@PathVariable(CONTROLLER.SLUG) String slug) throws NotFoundApiException {
    	
    	log.info("Processing delete of entity of slug: [{}]", slug);
    	hypothesisService.delete(slug);
		return new ResponseEntity<>(HttpStatus.OK);
    }
	
}