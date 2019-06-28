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
import com.uff.phenomanager.domain.ComputationalModel;
import com.uff.phenomanager.domain.core.ApiResponse;
import com.uff.phenomanager.domain.core.filter.RequestFilter;
import com.uff.phenomanager.domain.dto.amqp.ModelExecutionMessageDto;
import com.uff.phenomanager.exception.ApiException;
import com.uff.phenomanager.exception.NotFoundApiException;
import com.uff.phenomanager.service.ComputationalModelService;

@RestController
@RequestMapping(CONTROLLER.COMPUTATIONAL_MODEL.PATH)
public class ComputationalModelController {
	
	private static final Logger log = LoggerFactory.getLogger(ComputationalModelController.class);
	
	@Autowired
	private ComputationalModelService computationalModelService;
	
	@PostMapping(value = CONTROLLER.COMPUTATIONAL_MODEL.RUN_PATH, 
			consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	@PreAuthorize("@computationalModelService.allowPermissionWriteAccess(#authorization, #slug)")
    public ResponseEntity<Object> run(@RequestHeader(JWT_AUTH.AUTHORIZATION) String authorization,
    		@PathVariable(CONTROLLER.SLUG) String slug,
    		@RequestBody ModelExecutionMessageDto modelExecutionMessageDto) throws ApiException {
    	
		log.info("Processing Invoke ComputationalModel of slug: [{}], and modelExecutionMessageDto: [{}]", 
				slug, modelExecutionMessageDto);
		
    	computationalModelService.run(slug, authorization, modelExecutionMessageDto);
    	return new ResponseEntity<>(HttpStatus.OK);
    }
	
	@GetMapping(value = CONTROLLER.SLUG_PATH, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	@PreAuthorize("@computationalModelService.allowPermissionReadAccess(#authorization, #slug)")
    public ResponseEntity<ComputationalModel> getBySlug(@RequestHeader(JWT_AUTH.AUTHORIZATION) String authorization,
    		@PathVariable(CONTROLLER.SLUG) String slug) throws NotFoundApiException {
		log.info("Processing finOne by slug: [{}]", slug);
		return new ResponseEntity<>(computationalModelService.findBySlug(slug), HttpStatus.OK);
	}

    @GetMapping(produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<ApiResponse<ComputationalModel>> getAll(@RequestHeader(JWT_AUTH.AUTHORIZATION) String authorization,
    		@ModelAttribute("RequestFilter") RequestFilter requestFilter)  throws ApiException{
    	log.info("Finding Entities by requestFilter=[{}]", requestFilter);
		return new ResponseEntity<>(computationalModelService.findAll(requestFilter, authorization), HttpStatus.OK);
    }
    
    @PostMapping(consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<ComputationalModel> insert(@RequestHeader(JWT_AUTH.AUTHORIZATION) String authorization, 
    		@RequestBody ComputationalModel entity) throws ApiException {
    	
    	log.info("Processing insert of data: [{}]", entity);
		return new ResponseEntity<>(computationalModelService.save(entity, authorization), HttpStatus.OK);
    }
    
    @PutMapping(value = CONTROLLER.SLUG_PATH, 
			consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("@computationalModelService.allowPermissionWriteAccess(#authorization, #slug)")
    public ResponseEntity<ComputationalModel> update(@RequestHeader(JWT_AUTH.AUTHORIZATION) String authorization,
    		@PathVariable(CONTROLLER.SLUG) String slug, @RequestBody ComputationalModel entity) throws ApiException {
    	
    	log.info("Processing update of entity of slug: [{}]", slug);
		return new ResponseEntity<>(computationalModelService.update(entity), HttpStatus.OK);
    }
    
    @DeleteMapping(value = CONTROLLER.SLUG_PATH, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("@computationalModelService.allowPermissionWriteAccess(#authorization, #slug)")
    public ResponseEntity<Object> delete(@RequestHeader(JWT_AUTH.AUTHORIZATION) String authorization,
    		@PathVariable(CONTROLLER.SLUG) String slug) throws NotFoundApiException {
    	
    	log.info("Processing delete of entity of slug: [{}]", slug);
    	computationalModelService.delete(slug);
		return new ResponseEntity<>(HttpStatus.OK);
    }
	
}