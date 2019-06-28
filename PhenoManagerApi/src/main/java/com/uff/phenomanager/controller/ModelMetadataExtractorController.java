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
import com.uff.phenomanager.domain.ModelMetadataExtractor;
import com.uff.phenomanager.domain.core.ApiResponse;
import com.uff.phenomanager.domain.core.filter.FilterOperator;
import com.uff.phenomanager.domain.core.filter.RequestFilter;
import com.uff.phenomanager.exception.ApiException;
import com.uff.phenomanager.exception.NotFoundApiException;
import com.uff.phenomanager.service.ModelMetadataExtractorService;
import com.uff.phenomanager.util.FileUtils;

@RestController
@RequestMapping(CONTROLLER.MODEL_METADATA_EXTRACTOR.PATH)
public class ModelMetadataExtractorController {
	
	private static final Logger log = LoggerFactory.getLogger(ModelMetadataExtractorController.class);
	
	@Autowired
	private ModelMetadataExtractorService modelMetadataExtractorService;
	
	@GetMapping(value = CONTROLLER.MODEL_METADATA_EXTRACTOR.EXTRACTOR_NAME_PATH, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	@PreAuthorize("@modelMetadataExtractorService.allowPermissionReadAccess(#authorization, #computationalModelSlug)")
    public HttpEntity<byte[]> getExecutorBySlug(@RequestHeader(JWT_AUTH.AUTHORIZATION) String authorization,
    		@PathVariable(CONTROLLER.MODEL_METADATA_EXTRACTOR.COMPUTATIONAL_MODEL_SLUG) String computationalModelSlug,
    		@PathVariable(CONTROLLER.SLUG) String slug,
    		HttpServletResponse response) throws IOException, ApiException {
		
		log.info("Downloading extractor file of modelMetadataExtractor of slug [{}]; and computationalModelSlug: [{}]", slug, computationalModelSlug);

		ModelMetadataExtractor modelMetadataExtractor = modelMetadataExtractorService.findBySlug(slug, authorization);
		byte[] fileContent = modelMetadataExtractorService.getExtractor(modelMetadataExtractor.getExtractorFileId());
    	
		String tmpFilePath = FileUtils.buildTmpPath(modelMetadataExtractor.getExtractorFileName());
    	
    	FileOutputStream fos = new FileOutputStream(tmpFilePath);
    	fos.write(fileContent);
    	fos.close();

    	HttpHeaders headers = new HttpHeaders();
    	headers.setContentType(MediaType.valueOf(modelMetadataExtractor.getExtractorFileContentType()));
    	response.setHeader(DOWNLOAD.CONTENT_HEADER, 
    			String.format(DOWNLOAD.ATTACHMENT_HEADER, modelMetadataExtractor.getExtractorFileName()));
    	
		return new HttpEntity<byte[]>(fileContent, headers);
	}
    
    @PostMapping(value = CONTROLLER.MODEL_METADATA_EXTRACTOR.EXTRACTOR_NAME_PATH, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("@modelMetadataExtractorService.allowPermissionWriteAccess(#authorization, #computationalModelSlug)")
    public ResponseEntity<ModelMetadataExtractor> uploadExtractor(@RequestHeader(JWT_AUTH.AUTHORIZATION) String authorization, 
    		@PathVariable(CONTROLLER.MODEL_METADATA_EXTRACTOR.COMPUTATIONAL_MODEL_SLUG) String computationalModelSlug,
    		@PathVariable(CONTROLLER.SLUG) String slug,
    		@RequestParam(value = "extractor", required = false) MultipartFile extractor) throws ApiException, IOException {
    	
    	log.info("Processing upload of extractor for modelMetadataExtractor of slug [{}]; and computationalModelSlug: [{}]", slug, computationalModelSlug);
		return new ResponseEntity<>(modelMetadataExtractorService.uploadExtractor(slug, extractor), HttpStatus.OK);
    }
	
	@WithoutSecurity
	@GetMapping(value = CONTROLLER.SLUG_PATH, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<ModelMetadataExtractor> getBySlug(@RequestHeader(JWT_AUTH.AUTHORIZATION) String authorization,
    		@PathVariable(CONTROLLER.MODEL_METADATA_EXTRACTOR.COMPUTATIONAL_MODEL_SLUG) String computationalModelSlug,
    		@PathVariable(CONTROLLER.SLUG) String slug) throws ApiException {
		
		log.info("Processing finOne by slug: [{}]; and computationalModelSlug: [{}]", slug, computationalModelSlug);
		return (ResponseEntity<ModelMetadataExtractor>) new ResponseEntity<>(
				modelMetadataExtractorService.findBySlug(slug, authorization), HttpStatus.OK);
	}

    @GetMapping(produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<ApiResponse<ModelMetadataExtractor>> getAll(@RequestHeader(JWT_AUTH.AUTHORIZATION) String authorization,
    		@ModelAttribute("RequestFilter") RequestFilter requestFilter,
    		@PathVariable(CONTROLLER.MODEL_METADATA_EXTRACTOR.COMPUTATIONAL_MODEL_SLUG) String computationalModelSlug)  throws ApiException{
    	
    	log.info("Finding Entities by requestFilter=[{}]", requestFilter);
    	requestFilter.addAndFilter("computationalModel.slug", computationalModelSlug, FilterOperator.EQ);
		return new ResponseEntity<>(modelMetadataExtractorService.findAll(requestFilter, authorization), HttpStatus.OK);
    }
    
    @PostMapping(produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("@modelMetadataExtractorService.allowPermissionWriteAccess(#authorization, #computationalModelSlug)")
    public ResponseEntity<ModelMetadataExtractor> insert(@RequestHeader(JWT_AUTH.AUTHORIZATION) String authorization, 
    		@RequestBody ModelMetadataExtractor entity,
    		@PathVariable(CONTROLLER.MODEL_METADATA_EXTRACTOR.COMPUTATIONAL_MODEL_SLUG) String computationalModelSlug) throws ApiException {
    	
    	log.info("Processing insert of data: [{}]; and computationalModelSlug: [{}]", computationalModelSlug);
		return (ResponseEntity<ModelMetadataExtractor>) new ResponseEntity<>(modelMetadataExtractorService.save(entity, computationalModelSlug), HttpStatus.OK);
    }
    
    @PutMapping(value = CONTROLLER.SLUG_PATH, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("@modelMetadataExtractorService.allowPermissionWriteAccess(#authorization, #computationalModelSlug)")
    public ResponseEntity<ModelMetadataExtractor> update(@RequestHeader(JWT_AUTH.AUTHORIZATION) String authorization,
    		@PathVariable(CONTROLLER.SLUG) String slug, 
    		@PathVariable(CONTROLLER.MODEL_METADATA_EXTRACTOR.COMPUTATIONAL_MODEL_SLUG) String computationalModelSlug,
    		@RequestBody ModelMetadataExtractor entity) throws ApiException {
    	
    	log.info("Processing update of entity of slug: [{}]; and computationalModelSlug: [{}]", slug, computationalModelSlug);
		return (ResponseEntity<ModelMetadataExtractor>) new ResponseEntity<>(modelMetadataExtractorService.update(entity, computationalModelSlug), HttpStatus.OK);
    }
    
    @DeleteMapping(value = CONTROLLER.SLUG_PATH, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("@modelMetadataExtractorService.allowPermissionWriteAccess(#authorization, #slug)")
    public ResponseEntity<Object> delete(@RequestHeader(JWT_AUTH.AUTHORIZATION) String authorization,
    		@PathVariable(CONTROLLER.SLUG) String slug,
    		@PathVariable(CONTROLLER.MODEL_METADATA_EXTRACTOR.COMPUTATIONAL_MODEL_SLUG) String computationalModelSlug) throws NotFoundApiException {
    	
    	log.info("Processing delete of entity of slug: [{}] and computationalModelSlug: [{}]", slug, computationalModelSlug);
    	modelMetadataExtractorService.delete(slug);
		return new ResponseEntity<>(HttpStatus.OK);
    }
	
}