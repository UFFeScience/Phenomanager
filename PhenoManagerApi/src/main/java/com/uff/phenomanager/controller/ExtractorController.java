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
import com.uff.phenomanager.domain.Extractor;
import com.uff.phenomanager.domain.core.ApiResponse;
import com.uff.phenomanager.domain.core.filter.FilterOperator;
import com.uff.phenomanager.domain.core.filter.RequestFilter;
import com.uff.phenomanager.exception.ApiException;
import com.uff.phenomanager.service.ExtractorService;
import com.uff.phenomanager.util.FileUtils;

@RestController
@RequestMapping(CONTROLLER.EXTRACTOR.PATH)
public class ExtractorController {
	
	private static final Logger log = LoggerFactory.getLogger(ExtractorController.class);
	
	@Autowired
	private ExtractorService extractorService;
	
	@GetMapping(value = CONTROLLER.EXTRACTOR.EXTRACTOR_FILE_NAME_PATH, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	@PreAuthorize("@extractorService.allowPermissionReadAccess(#authorization, #computationalModelSlug)")
    public HttpEntity<byte[]> getExecutorBySlug(@RequestHeader(JWT_AUTH.AUTHORIZATION) String authorization,
    		@PathVariable(CONTROLLER.EXTRACTOR.COMPUTATIONAL_MODEL_SLUG) String computationalModelSlug,
    		@PathVariable(CONTROLLER.SLUG) String slug,
    		HttpServletResponse response) throws IOException, ApiException {
		
		log.info("Downloading extractor file of extractor of slug [{}]; and computationalModelSlug: [{}]", slug, computationalModelSlug);

		Extractor extractor = extractorService.findBySlug(slug, authorization, computationalModelSlug);
		byte[] fileContent = extractorService.getExtractor(extractor.getFileId());
    	
		String tmpFilePath = FileUtils.buildTmpPath(extractor.getFileName());
    	
    	FileOutputStream fos = new FileOutputStream(tmpFilePath);
    	fos.write(fileContent);
    	fos.close();

    	HttpHeaders headers = new HttpHeaders();
    	headers.setContentType(MediaType.valueOf(extractor.getFileContentType()));
    	response.setHeader(DOWNLOAD.CONTENT_HEADER, 
    			String.format(DOWNLOAD.ATTACHMENT_HEADER, extractor.getFileName()));
    	
		return new HttpEntity<byte[]>(fileContent, headers);
	}
    
    @PostMapping(value = CONTROLLER.EXTRACTOR.EXTRACTOR_FILE_NAME_PATH, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("@extractorService.allowPermissionWriteAccess(#authorization, #computationalModelSlug)")
    public ResponseEntity<Extractor> uploadExtractor(@RequestHeader(JWT_AUTH.AUTHORIZATION) String authorization, 
    		@PathVariable(CONTROLLER.EXTRACTOR.COMPUTATIONAL_MODEL_SLUG) String computationalModelSlug,
    		@PathVariable(CONTROLLER.SLUG) String slug,
    		@RequestParam(value = "extractorFile", required = false) MultipartFile extractorFile) throws ApiException, IOException {
    	
    	log.info("Processing upload of extractor for extractor of slug [{}]; and computationalModelSlug: [{}]", slug, computationalModelSlug);
		return new ResponseEntity<>(extractorService.uploadExtractor(slug, extractorFile), HttpStatus.OK);
    }
	
	@WithoutSecurity
	@GetMapping(value = CONTROLLER.SLUG_PATH, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<Extractor> getBySlug(@RequestHeader(JWT_AUTH.AUTHORIZATION) String authorization,
    		@PathVariable(CONTROLLER.EXTRACTOR.COMPUTATIONAL_MODEL_SLUG) String computationalModelSlug,
    		@PathVariable(CONTROLLER.SLUG) String slug) throws ApiException {
		
		log.info("Processing finOne by slug: [{}]; and computationalModelSlug: [{}]", slug, computationalModelSlug);
		return (ResponseEntity<Extractor>) new ResponseEntity<>(
				extractorService.findBySlug(slug, authorization, computationalModelSlug), HttpStatus.OK);
	}

    @GetMapping(produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<ApiResponse<Extractor>> getAll(@RequestHeader(JWT_AUTH.AUTHORIZATION) String authorization,
    		@ModelAttribute("RequestFilter") RequestFilter requestFilter,
    		@PathVariable(CONTROLLER.EXTRACTOR.COMPUTATIONAL_MODEL_SLUG) String computationalModelSlug)  throws ApiException{
    	
    	log.info("Finding Entities by requestFilter=[{}]", requestFilter);
    	requestFilter.addAndFilter("computationalModel.slug", computationalModelSlug, FilterOperator.EQ);
		return new ResponseEntity<>(extractorService.findAll(requestFilter, authorization), HttpStatus.OK);
    }
    
    @PostMapping(produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("@extractorService.allowPermissionWriteAccess(#authorization, #computationalModelSlug)")
    public ResponseEntity<Extractor> insert(@RequestHeader(JWT_AUTH.AUTHORIZATION) String authorization, 
    		@RequestBody Extractor entity,
    		@PathVariable(CONTROLLER.EXTRACTOR.COMPUTATIONAL_MODEL_SLUG) String computationalModelSlug) throws ApiException {
    	
    	log.info("Processing insert of data: [{}]; and computationalModelSlug: [{}]", computationalModelSlug);
		return (ResponseEntity<Extractor>) new ResponseEntity<>(extractorService.save(entity, computationalModelSlug), HttpStatus.OK);
    }
    
    @PutMapping(value = CONTROLLER.SLUG_PATH, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("@extractorService.allowPermissionWriteAccess(#authorization, #computationalModelSlug)")
    public ResponseEntity<Extractor> update(@RequestHeader(JWT_AUTH.AUTHORIZATION) String authorization,
    		@PathVariable(CONTROLLER.SLUG) String slug, 
    		@PathVariable(CONTROLLER.EXTRACTOR.COMPUTATIONAL_MODEL_SLUG) String computationalModelSlug,
    		@RequestBody Extractor entity) throws ApiException {
    	
    	log.info("Processing update of entity of slug: [{}]; and computationalModelSlug: [{}]", slug, computationalModelSlug);
		return (ResponseEntity<Extractor>) new ResponseEntity<>(extractorService.update(entity, computationalModelSlug), HttpStatus.OK);
    }
    
    @DeleteMapping(value = CONTROLLER.SLUG_PATH, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("@extractorService.allowPermissionWriteAccess(#authorization, #slug)")
    public ResponseEntity<Object> delete(@RequestHeader(JWT_AUTH.AUTHORIZATION) String authorization,
    		@PathVariable(CONTROLLER.SLUG) String slug,
    		@PathVariable(CONTROLLER.EXTRACTOR.COMPUTATIONAL_MODEL_SLUG) String computationalModelSlug) throws ApiException {
    	
    	log.info("Processing delete of entity of slug: [{}] and computationalModelSlug: [{}]", slug, computationalModelSlug);
    	extractorService.delete(slug);
		return new ResponseEntity<>(HttpStatus.OK);
    }
	
}