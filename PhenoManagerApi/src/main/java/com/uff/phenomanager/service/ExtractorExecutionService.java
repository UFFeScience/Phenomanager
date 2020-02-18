package com.uff.phenomanager.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.uff.phenomanager.Constants;
import com.uff.phenomanager.Constants.MSG_ERROR;
import com.uff.phenomanager.domain.ComputationalModel;
import com.uff.phenomanager.domain.Environment;
import com.uff.phenomanager.domain.ExecutionStatus;
import com.uff.phenomanager.domain.ExtractorExecution;
import com.uff.phenomanager.domain.Extractor;
import com.uff.phenomanager.domain.Execution;
import com.uff.phenomanager.exception.ApiException;
import com.uff.phenomanager.exception.NotFoundApiException;
import com.uff.phenomanager.exception.UnauthorizedApiException;
import com.uff.phenomanager.repository.ExtractorExecutionRepository;
import com.uff.phenomanager.service.api.google.GoogleDriveService;
import com.uff.phenomanager.service.core.ApiPermissionRestService;
import com.uff.phenomanager.util.TokenUtils;

@Service
public class ExtractorExecutionService extends ApiPermissionRestService<ExtractorExecution, ExtractorExecutionRepository> {
	
	private static final Logger log = LoggerFactory.getLogger(ExecutionService.class);
	
	@Autowired
	private ExtractorExecutionRepository extractorExecutionRepository;
	
	@Lazy
	@Autowired
	private GoogleDriveService googleDriveService;
	
	@Override
	protected ExtractorExecutionRepository getRepository() {
		return extractorExecutionRepository;
	}
	
	@Override
	protected Class<ExtractorExecution> getEntityClass() {
		return ExtractorExecution.class;
	}
	
	@Override
	protected String getPermissionEntityName() {
		return StringUtils.uncapitalize(ComputationalModel.class.getSimpleName());
	}
	
	public ExtractorExecution findBySlug(String slug, String authorization, String computationalModelSlug) throws ApiException {
		ExtractorExecution entity = findBySlug(slug);
		
		if (entity.getExtractor().getComputationalModel().getIsPublicData()) {
			return entity;
		}
		
		Boolean hasAuthorization = Boolean.TRUE;
		String token = TokenUtils.getTokenFromAuthorizationHeader(authorization);

		if (token == null || "".equals(token)) {
        	hasAuthorization = Boolean.FALSE;
		
        } else if (!tokenAuthenticationService.validateToken(token)) {
			hasAuthorization = Boolean.FALSE;
		
        } else if (!allowPermissionReadAccess(authorization, computationalModelSlug)) {
        	hasAuthorization = Boolean.FALSE;
        }
        
		if (!hasAuthorization) {
	       throw new UnauthorizedApiException(MSG_ERROR.AUTHORIZATION_TOKEN_NOT_VALID);
	    } 
		
		return entity;
	}

	public byte[] getExecutionMetadata(String fileId) throws NotFoundApiException {
		return googleDriveService.getFileBytesContent(fileId);
	}
	
	public Integer deleteByExecution(Execution execution) {
		List<ExtractorExecution> extractorExecutions = extractorExecutionRepository.findAllByExecution(execution);
		
		if (extractorExecutions == null || extractorExecutions.isEmpty()) {
			return 0;
		}
		
		Integer deletedResult = extractorExecutionRepository.deleteByExecution(execution);
		
		for (ExtractorExecution extractorExecution : extractorExecutions) {
			
			if (extractorExecution.getExecutionMetadataFileId() != null && !"".equals(extractorExecution.getExecutionMetadataFileId())) {
				try {
					googleDriveService.deleteFileAsync(extractorExecution.getExecutionMetadataFileId());
					
				} catch (NotFoundApiException e) {
					log.warn(Constants.MSG_WARN.GOOGLE_FILE_NOT_FOUND, extractorExecution.getExecutionMetadataFileId());
					continue;
				}
			}
		}
		
		return deletedResult;
	}
	
	public ExtractorExecution findByExtractorAndExecutionStatus(Extractor extractor, ExecutionStatus status) {
		return extractorExecutionRepository.findByExtractorAndExecutionStatus(extractor, status);
	}

	public Long countByExtractorAndExecutionStatus(Extractor extractor, ExecutionStatus status) {
		return extractorExecutionRepository.countByExtractorAndExecutionStatus(extractor, status);
	}

	public Long countByExtractorAndExecutionEnvironmentAndExecutionStatus(Extractor extractor, 
			Environment environment, ExecutionStatus status) {
		return extractorExecutionRepository
				.countByExtractorAndExecutionEnvironmentAndExecutionStatus(extractor, environment, status);
	}
	
}