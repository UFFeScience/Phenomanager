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
import com.uff.phenomanager.domain.ExtractorMetadata;
import com.uff.phenomanager.exception.ApiException;
import com.uff.phenomanager.service.ExtractorMetadataService;
import com.uff.phenomanager.util.FileUtils;

@RestController
@RequestMapping(CONTROLLER.EXTRACTOR_METADATA.PATH)
public class ExtractorMetadataController {
	
	private static final Logger log = LoggerFactory.getLogger(ExtractorMetadataController.class);
	
	@Autowired
	private ExtractorMetadataService extractorMetadataService;
	
	@GetMapping(value = CONTROLLER.EXTRACTOR_METADATA.EXECUTION_METADATA_NAME_PATH, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	@PreAuthorize("@modelMetadataExtractorService.allowPermissionReadAccess(#authorization, #computationalModelSlug)")
    public HttpEntity<byte[]> getExecutionMetadataBySlug(@RequestHeader(JWT_AUTH.AUTHORIZATION) String authorization,
    		@PathVariable(CONTROLLER.MODEL_METADATA_EXTRACTOR.COMPUTATIONAL_MODEL_SLUG) String computationalModelSlug,
    		@PathVariable(CONTROLLER.SLUG) String slug,
    		HttpServletResponse response) throws IOException, ApiException {
		
		log.info("Downloading executionMetadata file of extractorMetadata of slug [{}]; and computationalModelSlug: [{}]", 
				slug, computationalModelSlug);

		ExtractorMetadata extractorMetadata = extractorMetadataService.findBySlug(slug, authorization);
		byte[] fileContent = extractorMetadataService.getExecutionMetadata(extractorMetadata.getExecutionMetadataFileId());
    	
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
    public ResponseEntity<ExtractorMetadata> getBySlug(@RequestHeader(JWT_AUTH.AUTHORIZATION) String authorization,
    		@PathVariable(CONTROLLER.EXTRACTOR_METADATA.COMPUTATIONAL_MODEL_SLUG) String computationalModelSlug,
    		@PathVariable(CONTROLLER.SLUG) String slug) throws ApiException {
		
		log.info("Processing finOne by slug: [{}]; and computationalModelSlug: [{}]", slug, computationalModelSlug);
		return new ResponseEntity<>(extractorMetadataService.findBySlug(slug, authorization), HttpStatus.OK);
	}
	
}