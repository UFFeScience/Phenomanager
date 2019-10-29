package com.uff.phenomanager.controller;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.uff.phenomanager.Constants.CONTROLLER;
import com.uff.phenomanager.Constants.JWT_AUTH;
import com.uff.phenomanager.domain.dto.ValidationStatisticsDto;
import com.uff.phenomanager.exception.ApiException;
import com.uff.phenomanager.service.ModelResultMetadataService;
import com.uff.phenomanager.service.ValidationItemService;

@RestController
@RequestMapping(CONTROLLER.DASHBOARD.PATH)
public class DashboardController {
	
	private static final Logger log = LoggerFactory.getLogger(ComputationalModelController.class);
	
	@Autowired
	private ValidationItemService validationItemService;
	
	@Autowired
	private ModelResultMetadataService modelResultMetadataService;
	
	@GetMapping(value = CONTROLLER.DASHBOARD.VALIDATION_STATISTICS_NAME_PATH, 
			produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<ValidationStatisticsDto> getValidationStatistics(@RequestHeader(JWT_AUTH.AUTHORIZATION) String authorization) throws ApiException {
		log.info("Processing validation statistics");
    	return new ResponseEntity<>(validationItemService.getStatistics(authorization), HttpStatus.OK);
    }
	
	@GetMapping(value = CONTROLLER.DASHBOARD.RUNING_MODELS_PATH, 
			produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<Map<String, Long>> getCountRunningModels(@RequestHeader(JWT_AUTH.AUTHORIZATION) String authorization) throws ApiException {
		log.info("Processing count all models running");
    	return new ResponseEntity<>(modelResultMetadataService.countAllRunningModels(authorization), HttpStatus.OK);
    }
	
	@GetMapping(value = CONTROLLER.DASHBOARD.ERROR_MODELS_PATH, 
			produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<Map<String, Long>> getCountErrorModels(
    		@RequestHeader(JWT_AUTH.AUTHORIZATION) String authorization) throws ApiException {
		log.info("Processing count all models with error");
    	return new ResponseEntity<>(modelResultMetadataService.countAllErrorModels(authorization), HttpStatus.OK);
    }
	
}