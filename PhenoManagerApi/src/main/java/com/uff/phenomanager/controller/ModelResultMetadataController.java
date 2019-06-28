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
import com.uff.phenomanager.domain.ModelResultMetadata;
import com.uff.phenomanager.domain.core.ApiResponse;
import com.uff.phenomanager.domain.core.filter.FilterOperator;
import com.uff.phenomanager.domain.core.filter.RequestFilter;
import com.uff.phenomanager.exception.ApiException;
import com.uff.phenomanager.service.ModelResultMetadataService;
import com.uff.phenomanager.util.FileUtils;

@RestController
@RequestMapping(CONTROLLER.MODEL_RESULT_METADATA.PATH)
public class ModelResultMetadataController {
	
	private static final Logger log = LoggerFactory.getLogger(ModelResultMetadataController.class);
	
	@Autowired
	private ModelResultMetadataService modelResultMetadataService;
	
	@GetMapping(value = CONTROLLER.MODEL_RESULT_METADATA.EXECUTION_METADATA_NAME_PATH, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	@PreAuthorize("@modelResultMetadataService.allowPermissionReadAccess(#authorization, #computationalModelSlug)")
    public HttpEntity<byte[]> getExecutionMetadataBySlug(@RequestHeader(JWT_AUTH.AUTHORIZATION) String authorization,
    		@PathVariable(CONTROLLER.MODEL_RESULT_METADATA.COMPUTATIONAL_MODEL_SLUG) String computationalModelSlug,
    		@PathVariable(CONTROLLER.SLUG) String slug,
    		HttpServletResponse response) throws IOException, ApiException {
		
		log.info("Downloading executionMetadata of modelResultMetadata of slug [{}]; and computationalModelSlug: [{}]", slug, computationalModelSlug);

		ModelResultMetadata modelResultMetadata = modelResultMetadataService.findBySlug(slug, authorization, computationalModelSlug);
		byte[] fileContent = modelResultMetadataService.getExecutionMetadata(modelResultMetadata.getExecutionMetadataFileId());
    	
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
    
    @GetMapping(value = CONTROLLER.MODEL_RESULT_METADATA.ABORT_METADATA_NAME_PATH, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	@PreAuthorize("@modelResultMetadataService.allowPermissionReadAccess(#authorization, #computationalModelSlug)")
    public HttpEntity<byte[]> getAbortMetadataBySlug(@RequestHeader(JWT_AUTH.AUTHORIZATION) String authorization,
    		@PathVariable(CONTROLLER.MODEL_RESULT_METADATA.COMPUTATIONAL_MODEL_SLUG) String computationalModelSlug,
    		@PathVariable(CONTROLLER.SLUG) String slug,
    		HttpServletResponse response) throws IOException, ApiException {
		
		log.info("Downloading abortMetadata of modelResultMetadata of slug [{}]; and computationalModelSlug: [{}]", slug, computationalModelSlug);

		ModelResultMetadata modelResultMetadata = modelResultMetadataService.findBySlug(slug, authorization, computationalModelSlug);
		byte[] fileContent = modelResultMetadataService.getAbortMetadata(modelResultMetadata.getAbortMetadataFileId());
    	
		String metadataFileName = "abort-metadata.txt";
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
	@GetMapping(value = CONTROLLER.MODEL_RESULT_METADATA.RESEARCH_OBJECT_NAME_PATH, 
		produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<Map<String, Object>> getResearchObjectBySlug(@RequestHeader(JWT_AUTH.AUTHORIZATION) String authorization,
    		@PathVariable(CONTROLLER.MODEL_RESULT_METADATA.COMPUTATIONAL_MODEL_SLUG) String computationalModelSlug,
    		@PathVariable(CONTROLLER.SLUG) String slug) throws ApiException {
		log.info("Processing finOne by slug: [{}]", slug);
		return new ResponseEntity<Map<String, Object>>(
				modelResultMetadataService.getResearchObject(slug, authorization, computationalModelSlug), HttpStatus.OK);
	}
	
	@WithoutSecurity
	@GetMapping(value = CONTROLLER.SLUG_PATH, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<ModelResultMetadata> getBySlug(@RequestHeader(JWT_AUTH.AUTHORIZATION) String authorization,
    		@PathVariable(CONTROLLER.MODEL_RESULT_METADATA.COMPUTATIONAL_MODEL_SLUG) String computationalModelSlug,
    		@PathVariable(CONTROLLER.SLUG) String slug) throws ApiException {
		
		log.info("Processing finOne by slug: [{}]; and computationalModelSlug: [{}]", slug, computationalModelSlug);
		return (ResponseEntity<ModelResultMetadata>) new ResponseEntity<>(
				modelResultMetadataService.findBySlug(slug, authorization, computationalModelSlug), HttpStatus.OK);
	}

    @GetMapping(produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<ApiResponse<ModelResultMetadata>> getAll(@RequestHeader(JWT_AUTH.AUTHORIZATION) String authorization,
    		@ModelAttribute("RequestFilter") RequestFilter requestFilter,
    		@PathVariable(CONTROLLER.MODEL_RESULT_METADATA.COMPUTATIONAL_MODEL_SLUG) String computationalModelSlug)  throws ApiException{
    	
    	log.info("Finding Entities by requestFilter=[{}]", requestFilter);
    	requestFilter.addAndFilter("computationalModel.slug", computationalModelSlug, FilterOperator.EQ);
		return new ResponseEntity<>(modelResultMetadataService.findAll(requestFilter, authorization), HttpStatus.OK);
    }
	
}