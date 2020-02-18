package com.uff.phenomanager.controller;

import java.io.File;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.uff.phenomanager.Constants.CONTROLLER;
import com.uff.phenomanager.Constants.DOWNLOAD;
import com.uff.phenomanager.Constants.JWT_AUTH;
import com.uff.phenomanager.config.security.WithoutSecurity;
import com.uff.phenomanager.domain.ExtractorExecution;
import com.uff.phenomanager.exception.ApiException;
import com.uff.phenomanager.service.ExtractorExecutionService;
import com.uff.phenomanager.util.FileUtils;

@RestController
@RequestMapping(CONTROLLER.EXTRACTOR_EXECUTION.PATH)
public class ExtractorExecutionController {
	
	private static final Logger log = LoggerFactory.getLogger(ExtractorExecutionController.class);
	
	@Autowired
	private ExtractorExecutionService extractorExecutionService;
	
	@GetMapping(value = CONTROLLER.EXTRACTOR_EXECUTION.EXECUTION_METADATA_NAME_PATH, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	@PreAuthorize("@extractorExecutionService.allowPermissionReadAccess(#authorization, #computationalModelSlug)")
    public HttpEntity<byte[]> getExecutionMetadataBySlug(@RequestHeader(JWT_AUTH.AUTHORIZATION) String authorization,
    		@PathVariable(CONTROLLER.EXTRACTOR.COMPUTATIONAL_MODEL_SLUG) String computationalModelSlug,
    		@PathVariable(CONTROLLER.SLUG) String slug,
    		HttpServletResponse response) throws IOException, ApiException {
		
		log.info("Downloading executionMetadata file of extractorExecution of slug [{}]; and computationalModelSlug: [{}]", 
				slug, computationalModelSlug);

		ExtractorExecution extractorExecution = extractorExecutionService.findBySlug(slug, authorization, computationalModelSlug);
		byte[] fileContent = extractorExecutionService.getExecutionMetadata(extractorExecution.getExecutionMetadataFileId());
    	
		String metadataFileName = "execution-extraction-metadata";
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
	@GetMapping(value = CONTROLLER.SLUG_PATH, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<ExtractorExecution> getBySlug(@RequestHeader(JWT_AUTH.AUTHORIZATION) String authorization,
    		@PathVariable(CONTROLLER.EXTRACTOR_EXECUTION.COMPUTATIONAL_MODEL_SLUG) String computationalModelSlug,
    		@PathVariable(CONTROLLER.SLUG) String slug) throws ApiException {
		
		log.info("Processing finOne by slug: [{}]; and computationalModelSlug: [{}]", slug, computationalModelSlug);
		return new ResponseEntity<>(extractorExecutionService.findBySlug(slug, authorization, computationalModelSlug), HttpStatus.OK);
	}
	
}