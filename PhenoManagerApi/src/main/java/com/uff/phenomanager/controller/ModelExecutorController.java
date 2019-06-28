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
import com.uff.phenomanager.domain.ModelExecutor;
import com.uff.phenomanager.domain.core.ApiResponse;
import com.uff.phenomanager.domain.core.filter.FilterOperator;
import com.uff.phenomanager.domain.core.filter.RequestFilter;
import com.uff.phenomanager.exception.ApiException;
import com.uff.phenomanager.exception.NotFoundApiException;
import com.uff.phenomanager.service.ModelExecutorService;
import com.uff.phenomanager.util.FileUtils;

@RestController
@RequestMapping(CONTROLLER.MODEL_EXECUTOR.PATH)
public class ModelExecutorController {
	
	private static final Logger log = LoggerFactory.getLogger(ModelExecutorController.class);
	
	@Autowired
	private ModelExecutorService modelExecutorService;
	
	@GetMapping(value = CONTROLLER.MODEL_EXECUTOR.EXECUTOR_NAME_PATH, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	@PreAuthorize("@modelExecutorService.allowPermissionReadAccess(#authorization, #computationalModelSlug)")
    public HttpEntity<byte[]> getExecutorBySlug(@RequestHeader(JWT_AUTH.AUTHORIZATION) String authorization,
    		@PathVariable(CONTROLLER.MODEL_EXECUTOR.COMPUTATIONAL_MODEL_SLUG) String computationalModelSlug,
    		@PathVariable(CONTROLLER.SLUG) String slug,
    		HttpServletResponse response) throws IOException, ApiException {
		
		log.info("Downloading executor file of modelExecutor of slug [{}]; and computationalModelSlug: [{}]", slug, computationalModelSlug);

		ModelExecutor modelExecutor = modelExecutorService.findBySlug(slug, authorization);
		byte[] fileContent = modelExecutorService.getExecutor(modelExecutor.getExecutorFileId());
    	
		String tmpFilePath = FileUtils.buildTmpPath(modelExecutor.getExecutorFileName());
    	
    	FileOutputStream fos = new FileOutputStream(tmpFilePath);
    	fos.write(fileContent);
    	fos.close();

    	HttpHeaders headers = new HttpHeaders();
    	headers.setContentType(MediaType.valueOf(modelExecutor.getExecutorFileContentType()));
    	response.setHeader(DOWNLOAD.CONTENT_HEADER, 
    			String.format(DOWNLOAD.ATTACHMENT_HEADER, modelExecutor.getExecutorFileName()));
    	
		return new HttpEntity<byte[]>(fileContent, headers);
	}
    
    @PostMapping(value = CONTROLLER.MODEL_EXECUTOR.EXECUTOR_NAME_PATH, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("@modelExecutorService.allowPermissionWriteAccess(#authorization, #computationalModelSlug)")
    public ResponseEntity<ModelExecutor> uploadExecutor(@RequestHeader(JWT_AUTH.AUTHORIZATION) String authorization, 
    		@PathVariable(CONTROLLER.MODEL_EXECUTOR.COMPUTATIONAL_MODEL_SLUG) String computationalModelSlug,
    		@PathVariable(CONTROLLER.SLUG) String slug,
    		@RequestParam(value = "executor", required = false) MultipartFile executor) throws ApiException, IOException {
    	
    	log.info("Processing upload of executor for modelExecutor of slug [{}]; and computationalModelSlug: [{}]", slug, computationalModelSlug);
		return new ResponseEntity<>(modelExecutorService.uploadExecutor(slug, executor), HttpStatus.OK);
    }
	
	@WithoutSecurity
	@GetMapping(value = CONTROLLER.SLUG_PATH, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<ModelExecutor> getBySlug(@RequestHeader(JWT_AUTH.AUTHORIZATION) String authorization,
    		@PathVariable(CONTROLLER.MODEL_EXECUTOR.COMPUTATIONAL_MODEL_SLUG) String computationalModelSlug,
    		@PathVariable(CONTROLLER.SLUG) String slug) throws ApiException {
		
		log.info("Processing finOne by slug: [{}]; and computationalModelSlug: [{}]", slug, computationalModelSlug);
		return (ResponseEntity<ModelExecutor>) new ResponseEntity<>(modelExecutorService.findBySlug(slug, authorization), HttpStatus.OK);
	}

    @GetMapping(produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<ApiResponse<ModelExecutor>> getAll(@RequestHeader(JWT_AUTH.AUTHORIZATION) String authorization,
    		@ModelAttribute("RequestFilter") RequestFilter requestFilter,
    		@PathVariable(CONTROLLER.MODEL_EXECUTOR.COMPUTATIONAL_MODEL_SLUG) String computationalModelSlug)  throws ApiException{
    	
    	log.info("Finding Entities by requestFilter=[{}]", requestFilter);
    	requestFilter.addAndFilter("computationalModel.slug", computationalModelSlug, FilterOperator.EQ);
		return new ResponseEntity<>(modelExecutorService.findAll(requestFilter, authorization), HttpStatus.OK);
    }
    
    @PostMapping(produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("@modelExecutorService.allowPermissionWriteAccess(#authorization, #computationalModelSlug)")
    public ResponseEntity<ModelExecutor> insert(@RequestHeader(JWT_AUTH.AUTHORIZATION) String authorization, 
    		@RequestBody ModelExecutor entity,
    		@PathVariable(CONTROLLER.MODEL_EXECUTOR.COMPUTATIONAL_MODEL_SLUG) String computationalModelSlug) throws ApiException {
    	
    	log.info("Processing insert of data: [{}]; and computationalModelSlug: [{}]", computationalModelSlug);
		return (ResponseEntity<ModelExecutor>) new ResponseEntity<>(modelExecutorService.save(entity, computationalModelSlug), HttpStatus.OK);
    }
    
    @PutMapping(value = CONTROLLER.SLUG_PATH, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("@modelExecutorService.allowPermissionWriteAccess(#authorization, #computationalModelSlug)")
    public ResponseEntity<ModelExecutor> update(@RequestHeader(JWT_AUTH.AUTHORIZATION) String authorization,
    		@PathVariable(CONTROLLER.SLUG) String slug, 
    		@PathVariable(CONTROLLER.MODEL_EXECUTOR.COMPUTATIONAL_MODEL_SLUG) String computationalModelSlug,
    		@RequestBody ModelExecutor entity) throws ApiException {
    	
    	log.info("Processing update of entity of slug: [{}]; and computationalModelSlug: [{}]", slug, computationalModelSlug);
		return (ResponseEntity<ModelExecutor>) new ResponseEntity<>(modelExecutorService.update(entity, computationalModelSlug), HttpStatus.OK);
    }
    
    @DeleteMapping(value = CONTROLLER.SLUG_PATH, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("@modelExecutorService.allowPermissionWriteAccess(#authorization, #slug)")
    public ResponseEntity<Object> delete(@RequestHeader(JWT_AUTH.AUTHORIZATION) String authorization,
    		@PathVariable(CONTROLLER.SLUG) String slug,
    		@PathVariable(CONTROLLER.MODEL_EXECUTOR.COMPUTATIONAL_MODEL_SLUG) String computationalModelSlug) throws NotFoundApiException {
    	
    	log.info("Processing delete of entity of slug: [{}] and computationalModelSlug: [{}]", slug, computationalModelSlug);
    	modelExecutorService.delete(slug);
		return new ResponseEntity<>(HttpStatus.OK);
    }
	
}