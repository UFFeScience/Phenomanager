package com.uff.phenomanager.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;

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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.uff.phenomanager.Constants.CONTROLLER;
import com.uff.phenomanager.Constants.DOWNLOAD;
import com.uff.phenomanager.Constants.JWT_AUTH;
import com.uff.phenomanager.config.security.WithoutSecurity;
import com.uff.phenomanager.domain.Execution;
import com.uff.phenomanager.domain.core.ApiResponse;
import com.uff.phenomanager.domain.core.filter.FilterOperator;
import com.uff.phenomanager.domain.core.filter.RequestFilter;
import com.uff.phenomanager.exception.ApiException;
import com.uff.phenomanager.service.ExecutionService;
import com.uff.phenomanager.util.FileUtils;

@RestController
@RequestMapping(CONTROLLER.EXECUTION.PATH)
public class ExecutionController {
	
	private static final Logger log = LoggerFactory.getLogger(ExecutionController.class);
	
	@Autowired
	private ExecutionService executionService;
	
	@GetMapping(value = CONTROLLER.EXECUTION.EXECUTION_METADATA_NAME_PATH, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	@PreAuthorize("@executionService.allowPermissionReadAccess(#authorization, #computationalModelSlug)")
    public HttpEntity<byte[]> getExecutionMetadataBySlug(@RequestHeader(JWT_AUTH.AUTHORIZATION) String authorization,
    		@PathVariable(CONTROLLER.EXECUTION.COMPUTATIONAL_MODEL_SLUG) String computationalModelSlug,
    		@PathVariable(CONTROLLER.SLUG) String slug,
    		HttpServletResponse response) throws IOException, ApiException {
		
		log.info("Downloading executionMetadata of execution of slug [{}]; and computationalModelSlug: [{}]", slug, computationalModelSlug);

		Execution execution = executionService.findBySlug(slug, authorization, computationalModelSlug);
		byte[] fileContent = executionService.getExecutionMetadata(execution.getExecutionMetadataFileId());
    	
		String metadataFileName = "execution-metadata";
		String tmpFilePath = FileUtils.buildTmpPath(metadataFileName);
    	
    	FileOutputStream fos = new FileOutputStream(tmpFilePath);
    	fos.write(fileContent);
    	fos.close();

    	HttpHeaders headers = new HttpHeaders();
    	headers.setContentType(MediaType.valueOf(FileUtils.identifyFileType(new File(tmpFilePath))));
    	response.setHeader(DOWNLOAD.CONTENT_HEADER, 
    			String.format(DOWNLOAD.ATTACHMENT_HEADER, metadataFileName));
    	
		return new HttpEntity<byte[]>(fileContent, headers);
	}
    
    @GetMapping(value = CONTROLLER.EXECUTION.ABORTION_METADATA_NAME_PATH, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	@PreAuthorize("@executionService.allowPermissionReadAccess(#authorization, #computationalModelSlug)")
    public HttpEntity<byte[]> getAbortionMetadataBySlug(@RequestHeader(JWT_AUTH.AUTHORIZATION) String authorization,
    		@PathVariable(CONTROLLER.EXECUTION.COMPUTATIONAL_MODEL_SLUG) String computationalModelSlug,
    		@PathVariable(CONTROLLER.SLUG) String slug,
    		HttpServletResponse response) throws IOException, ApiException {
		
		log.info("Downloading abortionMetadata of execution of slug [{}]; and computationalModelSlug: [{}]", slug, computationalModelSlug);

		Execution execution = executionService.findBySlug(slug, authorization, computationalModelSlug);
		byte[] fileContent = executionService.getAbortMetadata(execution.getAbortionMetadataFileId());
    	
		String metadataFileName = "abortion-metadata.txt";
		String tmpFilePath = FileUtils.buildTmpPath(metadataFileName);
    	
    	FileOutputStream fos = new FileOutputStream(tmpFilePath);
    	fos.write(fileContent);
    	fos.close();

    	HttpHeaders headers = new HttpHeaders();
    	headers.setContentType(MediaType.valueOf(FileUtils.identifyFileType(new File(tmpFilePath))));
    	response.setHeader(DOWNLOAD.CONTENT_HEADER, 
    			String.format(DOWNLOAD.ATTACHMENT_HEADER, metadataFileName));
    	
		return new HttpEntity<byte[]>(fileContent, headers);
	}
	
	@WithoutSecurity
	@GetMapping(value = CONTROLLER.EXECUTION.RESEARCH_OBJECT_NAME_PATH, 
		produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<Map<String, Object>> getResearchObjectBySlug(@RequestHeader(JWT_AUTH.AUTHORIZATION) String authorization,
    		@PathVariable(CONTROLLER.EXECUTION.COMPUTATIONAL_MODEL_SLUG) String computationalModelSlug,
    		@PathVariable(CONTROLLER.SLUG) String slug) throws ApiException {
		log.info("Processing finOne by slug: [{}]", slug);
		return new ResponseEntity<Map<String, Object>>(
				executionService.getResearchObject(slug, authorization, computationalModelSlug), HttpStatus.OK);
	}
	
	@WithoutSecurity
	@GetMapping(value = CONTROLLER.SLUG_PATH, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<Execution> getBySlug(@RequestHeader(JWT_AUTH.AUTHORIZATION) String authorization,
    		@PathVariable(CONTROLLER.EXECUTION.COMPUTATIONAL_MODEL_SLUG) String computationalModelSlug,
    		@PathVariable(CONTROLLER.SLUG) String slug) throws ApiException {
		
		log.info("Processing finOne by slug: [{}]; and computationalModelSlug: [{}]", slug, computationalModelSlug);
		return (ResponseEntity<Execution>) new ResponseEntity<>(
				executionService.findBySlug(slug, authorization, computationalModelSlug), HttpStatus.OK);
	}

    @GetMapping(produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<ApiResponse<Execution>> getAll(@RequestHeader(JWT_AUTH.AUTHORIZATION) String authorization,
    		@ModelAttribute("RequestFilter") RequestFilter requestFilter,
    		@PathVariable(CONTROLLER.EXECUTION.COMPUTATIONAL_MODEL_SLUG) String computationalModelSlug)  throws ApiException{
    	
    	log.info("Finding Entities by requestFilter=[{}]", requestFilter);
    	requestFilter.addAndFilter("computationalModel.slug", computationalModelSlug, FilterOperator.EQ);
		return new ResponseEntity<>(executionService.findAll(requestFilter, authorization), HttpStatus.OK);
    }
	
}