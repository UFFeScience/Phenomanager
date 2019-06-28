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
import com.uff.phenomanager.domain.ExtractorMetadata;
import com.uff.phenomanager.domain.ModelResultMetadata;
import com.uff.phenomanager.exception.ApiException;
import com.uff.phenomanager.exception.NotFoundApiException;
import com.uff.phenomanager.exception.UnauthorizedApiException;
import com.uff.phenomanager.repository.ExtractorMetadataRepository;
import com.uff.phenomanager.service.api.google.GoogleDriveService;
import com.uff.phenomanager.service.core.ApiPermissionRestService;
import com.uff.phenomanager.util.TokenUtils;

@Service
public class ExtractorMetadataService extends ApiPermissionRestService<ExtractorMetadata, ExtractorMetadataRepository> {
	
	private static final Logger log = LoggerFactory.getLogger(ModelResultMetadataService.class);
	
	@Autowired
	private ExtractorMetadataRepository extractorMetadataRepository;
	
	@Lazy
	@Autowired
	private GoogleDriveService googleDriveService;
	
	@Override
	protected ExtractorMetadataRepository getRepository() {
		return extractorMetadataRepository;
	}
	
	@Override
	protected Class<ExtractorMetadata> getEntityClass() {
		return ExtractorMetadata.class;
	}
	
	@Override
	protected String getPermissionEntityName() {
		return StringUtils.uncapitalize(ComputationalModel.class.getSimpleName());
	}
	
	public ExtractorMetadata findBySlug(String slug, String authorization) throws ApiException {
		ExtractorMetadata entity = findBySlug(slug);
		
		if (entity.getModelMetadataExtractor().getComputationalModel().getIsPublicData()) {
			return entity;
		}
		
		Boolean hasAuthorization = Boolean.TRUE;
		String token = TokenUtils.getTokenFromAuthorizationHeader(authorization);

		if (token == null || "".equals(token)) {
        	hasAuthorization = Boolean.FALSE;
		
        } else if (!tokenAuthenticationService.validateToken(token)) {
			hasAuthorization = Boolean.FALSE;
		
        } else if (!allowPermissionReadAccess(authorization, slug)) {
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
	
	public Integer deleteByModelResultMetadata(ModelResultMetadata modelResultMetadata) {
		List<ExtractorMetadata> extractorMetadatas = extractorMetadataRepository.findAllByModelResultMetadata(modelResultMetadata);
		
		if (extractorMetadatas == null || extractorMetadatas.isEmpty()) {
			return 0;
		}
		
		Integer deletedResult = extractorMetadataRepository.deleteByModelResultMetadata(modelResultMetadata);
		
		for (ExtractorMetadata extractorMetadata : extractorMetadatas) {
			
			if (extractorMetadata.getExecutionMetadataFileId() != null && !"".equals(extractorMetadata.getExecutionMetadataFileId())) {
				try {
					googleDriveService.deleteFileAsync(extractorMetadata.getExecutionMetadataFileId());
					
				} catch (NotFoundApiException e) {
					log.warn(Constants.MSG_WARN.GOOGLE_FILE_NOT_FOUND, extractorMetadata.getExecutionMetadataFileId());
					continue;
				}
			}
		}
		
		return deletedResult;
	}
	
}