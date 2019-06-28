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
import com.uff.phenomanager.config.security.WithoutSecurity;
import com.uff.phenomanager.domain.InstanceParam;
import com.uff.phenomanager.domain.core.ApiResponse;
import com.uff.phenomanager.domain.core.filter.FilterOperator;
import com.uff.phenomanager.domain.core.filter.RequestFilter;
import com.uff.phenomanager.exception.ApiException;
import com.uff.phenomanager.exception.NotFoundApiException;
import com.uff.phenomanager.service.InstanceParamService;
import com.uff.phenomanager.util.FileUtils;

@RestController
@RequestMapping(CONTROLLER.INSTANCE_PARAM.PATH)
public class InstanceParamController {
	
	private static final Logger log = LoggerFactory.getLogger(InstanceParamController.class);
	
	@Autowired
	private InstanceParamService instanceParamService;
	
	@GetMapping(value = CONTROLLER.INSTANCE_PARAM.VALUE_FILE_NAME_PATH, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	@PreAuthorize("@instanceParamService.allowPermissionReadAccess(#authorization, #computationalModelSlug)")
    public HttpEntity<byte[]> getValueFileBySlug(@RequestHeader(JWT_AUTH.AUTHORIZATION) String authorization,
    		@PathVariable(CONTROLLER.INSTANCE_PARAM.COMPUTATIONAL_MODEL_SLUG) String computationalModelSlug,
    		@PathVariable(CONTROLLER.SLUG) String slug,
    		HttpServletResponse response) throws IOException, ApiException {
		
		log.info("Downloading value file of instanceParam of slug [{}]; and computationalModelSlug: [{}]", slug, computationalModelSlug);

		InstanceParam instanceParam = instanceParamService.findBySlug(slug, authorization);
		byte[] fileContent = instanceParamService.getValueFile(instanceParam.getValueFileId());
		
		String tmpFilePath = FileUtils.buildTmpPath(instanceParam.getValueFileName());
    	
    	FileOutputStream fos = new FileOutputStream(tmpFilePath);
    	fos.write(fileContent);
    	fos.close();

    	HttpHeaders headers = new HttpHeaders();
    	headers.setContentType(MediaType.valueOf(instanceParam.getValueFileContentType()));
    	response.setHeader(DOWNLOAD.CONTENT_HEADER, 
    			String.format(DOWNLOAD.ATTACHMENT_HEADER, instanceParam.getValueFileName()));
    	
		return new HttpEntity<byte[]>(fileContent, headers);
	}
    
    @PostMapping(value = CONTROLLER.INSTANCE_PARAM.VALUE_FILE_NAME_PATH, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("@instanceParamService.allowPermissionWriteAccess(#authorization, #computationalModelSlug)")
    public ResponseEntity<InstanceParam> uploadValueFile(@RequestHeader(JWT_AUTH.AUTHORIZATION) String authorization, 
    		@PathVariable(CONTROLLER.INSTANCE_PARAM.COMPUTATIONAL_MODEL_SLUG) String computationalModelSlug,
    		@PathVariable(CONTROLLER.SLUG) String slug,
    		@RequestParam(value = "valueFile", required = false) MultipartFile valueFile) throws ApiException, IOException {
    	
    	log.info("Processing upload of valueFile for instanceParam of slug [{}]; and computationalModelSlug: [{}]", slug, computationalModelSlug);
		return new ResponseEntity<>(instanceParamService.uploadValueFile(slug, valueFile), HttpStatus.OK);
    }
	
	@WithoutSecurity
	@GetMapping(value = CONTROLLER.SLUG_PATH, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<InstanceParam> getBySlug(@RequestHeader(JWT_AUTH.AUTHORIZATION) String authorization,
    		@PathVariable(CONTROLLER.INSTANCE_PARAM.COMPUTATIONAL_MODEL_SLUG) String computationalModelSlug,
    		@PathVariable(CONTROLLER.SLUG) String slug) throws ApiException {
		
		log.info("Processing finOne by slug: [{}]; and computationalModelSlug: [{}]", slug, computationalModelSlug);
		return (ResponseEntity<InstanceParam>) new ResponseEntity<>(instanceParamService.findBySlug(slug, authorization), HttpStatus.OK);
	}

    @GetMapping(produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<ApiResponse<InstanceParam>> getAll(@RequestHeader(JWT_AUTH.AUTHORIZATION) String authorization,
    		@ModelAttribute("RequestFilter") RequestFilter requestFilter,
    		@PathVariable(CONTROLLER.INSTANCE_PARAM.COMPUTATIONAL_MODEL_SLUG) String computationalModelSlug)  throws ApiException{
    	
    	log.info("Finding Entities by requestFilter=[{}]", requestFilter);
    	requestFilter.addAndFilter("computationalModel.slug", computationalModelSlug, FilterOperator.EQ);
		return new ResponseEntity<>(instanceParamService.findAll(requestFilter, authorization), HttpStatus.OK);
    }
    
    @PostMapping(produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("@instanceParamService.allowPermissionWriteAccess(#authorization, #computationalModelSlug)")
    public ResponseEntity<InstanceParam> insert(@RequestHeader(JWT_AUTH.AUTHORIZATION) String authorization, 
    		@RequestBody InstanceParam entity,
    		@PathVariable(CONTROLLER.INSTANCE_PARAM.COMPUTATIONAL_MODEL_SLUG) String computationalModelSlug) throws ApiException {
    	
    	log.info("Processing insert of data: [{}]; and computationalModelSlug: [{}]", computationalModelSlug);
		return (ResponseEntity<InstanceParam>) new ResponseEntity<>(instanceParamService.save(entity, computationalModelSlug), HttpStatus.OK);
    }
    
    @PutMapping(value = CONTROLLER.SLUG_PATH, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("@instanceParamService.allowPermissionWriteAccess(#authorization, #computationalModelSlug)")
    public ResponseEntity<InstanceParam> update(@RequestHeader(JWT_AUTH.AUTHORIZATION) String authorization,
    		@PathVariable(CONTROLLER.SLUG) String slug, 
    		@PathVariable(CONTROLLER.INSTANCE_PARAM.COMPUTATIONAL_MODEL_SLUG) String computationalModelSlug,
    		@RequestBody InstanceParam entity) throws ApiException {
    	
    	log.info("Processing update of entity of slug: [{}]; and computationalModelSlug: [{}]", slug, computationalModelSlug);
		return (ResponseEntity<InstanceParam>) new ResponseEntity<>(instanceParamService.update(entity, computationalModelSlug), HttpStatus.OK);
    }
    
    @DeleteMapping(value = CONTROLLER.SLUG_PATH, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("@instanceParamService.allowPermissionWriteAccess(#authorization, #slug)")
    public ResponseEntity<Object> delete(@RequestHeader(JWT_AUTH.AUTHORIZATION) String authorization,
    		@PathVariable(CONTROLLER.SLUG) String slug,
    		@PathVariable(CONTROLLER.INSTANCE_PARAM.COMPUTATIONAL_MODEL_SLUG) String computationalModelSlug) throws NotFoundApiException {
    	
    	log.info("Processing delete of entity of slug: [{}] and computationalModelSlug: [{}]", slug, computationalModelSlug);
    	instanceParamService.delete(slug);
		return new ResponseEntity<>(HttpStatus.OK);
    }
	
}