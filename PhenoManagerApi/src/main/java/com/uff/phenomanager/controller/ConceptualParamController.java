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
import com.uff.phenomanager.domain.ConceptualParam;
import com.uff.phenomanager.domain.core.ApiResponse;
import com.uff.phenomanager.domain.core.filter.FilterOperator;
import com.uff.phenomanager.domain.core.filter.RequestFilter;
import com.uff.phenomanager.exception.ApiException;
import com.uff.phenomanager.exception.NotFoundApiException;
import com.uff.phenomanager.service.ConceptualParamService;

@RestController
@RequestMapping(CONTROLLER.CONCEPTUAL_PARAM.PATH)
public class ConceptualParamController {
	
	private static final Logger log = LoggerFactory.getLogger(ConceptualParamController.class);
	
	@Autowired
	private ConceptualParamService conceptualParamService;
	
	@GetMapping(value = CONTROLLER.SLUG_PATH, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	@PreAuthorize("@conceptualParamService.allowPermissionReadAccess(#authorization, #experimentSlug)")
    public ResponseEntity<ConceptualParam> getBySlug(@RequestHeader(JWT_AUTH.AUTHORIZATION) String authorization,
    		@PathVariable(CONTROLLER.CONCEPTUAL_PARAM.EXPERIMENT_SLUG) String experimentSlug,
    		@PathVariable(CONTROLLER.SLUG) String slug) throws NotFoundApiException {
		
		log.info("Processing finOne by slug: [{}]; and experimentSlug: [{}]", slug, experimentSlug);
		return (ResponseEntity<ConceptualParam>) new ResponseEntity<>(conceptualParamService.findBySlug(slug), HttpStatus.OK);
	}

    @GetMapping(produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<ApiResponse<ConceptualParam>> getAll(@RequestHeader(JWT_AUTH.AUTHORIZATION) String authorization,
    		@ModelAttribute("RequestFilter") RequestFilter requestFilter,
    		@PathVariable(CONTROLLER.CONCEPTUAL_PARAM.EXPERIMENT_SLUG) String experimentSlug)  throws ApiException{
    	
    	log.info("Finding Entities by requestFilter=[{}]", requestFilter);
    	requestFilter.addAndFilter("experiment.slug", experimentSlug, FilterOperator.EQ);
		return new ResponseEntity<>(conceptualParamService.findAll(requestFilter, authorization), HttpStatus.OK);
    }
    
    @PostMapping(consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("@conceptualParamService.allowPermissionWriteAccess(#authorization, #experimentSlug)")
    public ResponseEntity<ConceptualParam> insert(@RequestHeader(JWT_AUTH.AUTHORIZATION) String authorization, 
    		@RequestBody ConceptualParam entity,
    		@PathVariable(CONTROLLER.CONCEPTUAL_PARAM.EXPERIMENT_SLUG) String experimentSlug) throws ApiException {
    	
    	log.info("Processing insert of data: [{}]; and experimentSlug: [{}]", experimentSlug);
		return (ResponseEntity<ConceptualParam>) new ResponseEntity<>(conceptualParamService.save(entity, experimentSlug), HttpStatus.OK);
    }
    
    @PutMapping(value = CONTROLLER.SLUG_PATH, 
			consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("@conceptualParamService.allowPermissionWriteAccess(#authorization, #experimentSlug)")
    public ResponseEntity<ConceptualParam> update(@RequestHeader(JWT_AUTH.AUTHORIZATION) String authorization,
    		@PathVariable(CONTROLLER.SLUG) String slug, 
    		@PathVariable(CONTROLLER.CONCEPTUAL_PARAM.EXPERIMENT_SLUG) String experimentSlug,
    		@RequestBody ConceptualParam entity) throws ApiException {
    	
    	log.info("Processing update of entity of slug: [{}]; and experimentSlug: [{}]", slug, experimentSlug);
		return (ResponseEntity<ConceptualParam>) new ResponseEntity<>(conceptualParamService.update(entity, experimentSlug), HttpStatus.OK);
    }
    
    @DeleteMapping(value = CONTROLLER.SLUG_PATH, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("@conceptualParamService.allowPermissionWriteAccess(#authorization, #slug)")
    public ResponseEntity<Object> delete(@RequestHeader(JWT_AUTH.AUTHORIZATION) String authorization,
    		@PathVariable(CONTROLLER.SLUG) String slug,
    		@PathVariable(CONTROLLER.CONCEPTUAL_PARAM.EXPERIMENT_SLUG) String experimentSlug) throws NotFoundApiException {
    	
    	log.info("Processing delete of entity of slug: [{}] and experimentSlug: [{}]", slug, experimentSlug);
    	conceptualParamService.delete(slug);
		return new ResponseEntity<>(HttpStatus.OK);
    }
	
}