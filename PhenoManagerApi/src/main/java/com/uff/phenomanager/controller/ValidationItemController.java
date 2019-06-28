package com.uff.phenomanager.controller;

import java.io.FileOutputStream;
import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.uff.phenomanager.Constants.CONTROLLER;
import com.uff.phenomanager.Constants.DOWNLOAD;
import com.uff.phenomanager.Constants.JWT_AUTH;
import com.uff.phenomanager.domain.ValidationItem;
import com.uff.phenomanager.domain.core.ApiResponse;
import com.uff.phenomanager.domain.core.filter.FilterOperator;
import com.uff.phenomanager.domain.core.filter.RequestFilter;
import com.uff.phenomanager.exception.ApiException;
import com.uff.phenomanager.exception.NotFoundApiException;
import com.uff.phenomanager.service.ValidationItemService;
import com.uff.phenomanager.util.FileUtils;

@RestController
@RequestMapping(CONTROLLER.VALIDATION_ITEM.PATH)
public class ValidationItemController {
	
	private static final Logger log = LoggerFactory.getLogger(ValidationItemController.class);
	
	@Autowired
	private ValidationItemService validationItemService;
	
	@GetMapping(value = CONTROLLER.VALIDATION_ITEM.VALIDATION_EVIDENCE_PATH, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	@PreAuthorize("@validationItemService.allowPermissionReadAccess(#authorization, #experimentSlug)")
    public HttpEntity<byte[]> getValidationEvidenceBySlug(@RequestHeader(JWT_AUTH.AUTHORIZATION) String authorization,
    		@PathVariable(CONTROLLER.VALIDATION_ITEM.EXPERIMENT_SLUG) String experimentSlug,
    		@PathVariable(CONTROLLER.SLUG) String slug,
    		HttpServletResponse response) throws NotFoundApiException, IOException {
		
		log.info("Downloading validation evidence file of validationItem of slug [{}]; and experimentSlug: [{}]", slug, experimentSlug);

		ValidationItem validationItem = validationItemService.findBySlug(slug);
		byte[] fileContent = validationItemService.getValidationEvidence(validationItem.getValidationEvidenceFileId());
    	
		String tmpFilePath = FileUtils.buildTmpPath(validationItem.getValidationEvidenceFileName());
    	
    	FileOutputStream fos = new FileOutputStream(tmpFilePath);
    	fos.write(fileContent);
    	fos.close();

    	HttpHeaders headers = new HttpHeaders();
    	headers.setContentType(MediaType.valueOf(validationItem.getValidationEvidenceFileContentType()));
    	response.setHeader(DOWNLOAD.CONTENT_HEADER, 
    			String.format(DOWNLOAD.ATTACHMENT_HEADER, validationItem.getValidationEvidenceFileName()));
    	
		return new HttpEntity<byte[]>(fileContent, headers);
	}
    
    @PostMapping(value = CONTROLLER.VALIDATION_ITEM.VALIDATION_EVIDENCE_PATH, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("@validationItemService.allowPermissionWriteAccess(#authorization, #experimentSlug)")
    public ResponseEntity<ValidationItem> uploadValidationEvidence(@RequestHeader(JWT_AUTH.AUTHORIZATION) String authorization, 
    		@PathVariable(CONTROLLER.VALIDATION_ITEM.EXPERIMENT_SLUG) String experimentSlug,
    		@PathVariable(CONTROLLER.SLUG) String slug,
    		@RequestParam(value = "validationEvidence", required = false) MultipartFile validationEvidence) throws ApiException, IOException {
    	
    	log.info("Processing upload of evidence for validationItem of slug [{}]; and experimentSlug: [{}]", slug, experimentSlug);
		return new ResponseEntity<>(validationItemService.uploadValidationEvidence(slug, validationEvidence), HttpStatus.OK);
    }
	
	@GetMapping(value = CONTROLLER.SLUG_PATH, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	@PreAuthorize("@validationItemService.allowPermissionReadAccess(#authorization, #experimentSlug)")
    public ResponseEntity<ValidationItem> getBySlug(@RequestHeader(JWT_AUTH.AUTHORIZATION) String authorization,
    		@PathVariable(CONTROLLER.VALIDATION_ITEM.EXPERIMENT_SLUG) String experimentSlug,
    		@PathVariable(CONTROLLER.SLUG) String slug) throws NotFoundApiException {
		
		log.info("Processing finOne by slug: [{}]; and experimentSlug: [{}]", slug, experimentSlug);
		return new ResponseEntity<>(validationItemService.findBySlug(slug), HttpStatus.OK);
	}

    @GetMapping(produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<ApiResponse<ValidationItem>> getAll(@RequestHeader(JWT_AUTH.AUTHORIZATION) String authorization,
    		@ModelAttribute("RequestFilter") RequestFilter requestFilter,
    		@PathVariable(CONTROLLER.VALIDATION_ITEM.EXPERIMENT_SLUG) String experimentSlug)  throws ApiException{
    	
    	log.info("Finding Entities by requestFilter=[{}]", requestFilter);
    	requestFilter.addAndFilter("experiment.slug", experimentSlug, FilterOperator.EQ);
		return new ResponseEntity<>(validationItemService.findAll(requestFilter, authorization), HttpStatus.OK);
    }
    
    @PostMapping(produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("@validationItemService.allowPermissionWriteAccess(#authorization, #experimentSlug)")
    public ResponseEntity<ValidationItem> insert(@RequestHeader(JWT_AUTH.AUTHORIZATION) String authorization, 
    		@RequestBody ValidationItem entity,
    		@PathVariable(CONTROLLER.VALIDATION_ITEM.EXPERIMENT_SLUG) String experimentSlug) throws ApiException {
    	
    	log.info("Processing insert of data: [{}]; and experimentSlug: [{}]", experimentSlug);
		return new ResponseEntity<>(validationItemService.save(entity, experimentSlug), HttpStatus.OK);
    }
    
    @PutMapping(value = CONTROLLER.SLUG_PATH, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("@validationItemService.allowPermissionWriteAccess(#authorization, #experimentSlug)")
    public ResponseEntity<ValidationItem> update(@RequestHeader(JWT_AUTH.AUTHORIZATION) String authorization,
    		@PathVariable(CONTROLLER.SLUG) String slug, 
    		@PathVariable(CONTROLLER.VALIDATION_ITEM.EXPERIMENT_SLUG) String experimentSlug,
    		@RequestBody ValidationItem entity) throws ApiException {
    	
    	log.info("Processing update of entity of slug: [{}]; and experimentSlug: [{}]", slug, experimentSlug);
		return new ResponseEntity<>(validationItemService.update(entity, experimentSlug), HttpStatus.OK);
    }
    
    @DeleteMapping(value = CONTROLLER.SLUG_PATH, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("@validationItemService.allowPermissionWriteAccess(#authorization, #slug)")
    public ResponseEntity<Object> delete(@RequestHeader(JWT_AUTH.AUTHORIZATION) String authorization,
    		@PathVariable(CONTROLLER.SLUG) String slug,
    		@PathVariable(CONTROLLER.VALIDATION_ITEM.EXPERIMENT_SLUG) String experimentSlug) throws NotFoundApiException {
    	
    	log.info("Processing delete of entity of slug: [{}] and experimentSlug: [{}]", slug, experimentSlug);
    	validationItemService.delete(slug);
		return new ResponseEntity<>(HttpStatus.OK);
    }
	
}