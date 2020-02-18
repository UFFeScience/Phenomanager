package com.uff.phenomanager.service;

import java.io.IOException;
import java.util.List;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.uff.phenomanager.Constants;
import com.uff.phenomanager.Constants.MSG_ERROR;
import com.uff.phenomanager.Constants.UPLOAD;
import com.uff.phenomanager.domain.ComputationalModel;
import com.uff.phenomanager.domain.ExecutionStatus;
import com.uff.phenomanager.domain.Experiment;
import com.uff.phenomanager.domain.Extractor;
import com.uff.phenomanager.domain.api.google.DriveFile;
import com.uff.phenomanager.exception.ApiException;
import com.uff.phenomanager.exception.BadRequestApiException;
import com.uff.phenomanager.exception.NotFoundApiException;
import com.uff.phenomanager.exception.UnauthorizedApiException;
import com.uff.phenomanager.repository.ExtractorRepository;
import com.uff.phenomanager.service.api.google.GoogleDriveService;
import com.uff.phenomanager.service.core.ApiPermissionRestService;
import com.uff.phenomanager.util.FileUtils;
import com.uff.phenomanager.util.KeyUtils;
import com.uff.phenomanager.util.StringParserUtils;
import com.uff.phenomanager.util.TokenUtils;

@Service
public class ExtractorService extends ApiPermissionRestService<Extractor, ExtractorRepository> {
	
	private static final Logger log = LoggerFactory.getLogger(ExtractorService.class);
	
	@Autowired
	private ExtractorRepository ExtractorRepository;
	
	@Autowired
	private ComputationalModelService computationalModelService;
	
	@Autowired
	private ExtractorExecutionService extractorExecutionService;
	
	@Lazy
	@Autowired
	private GoogleDriveService googleDriveService;
	
	@Override
	protected ExtractorRepository getRepository() {
		return ExtractorRepository;
	}
	
	@Override
	protected Class<Extractor> getEntityClass() {
		return Extractor.class;
	}
	
	@Override
	protected String getPermissionEntityName() {
		return StringUtils.uncapitalize(ComputationalModel.class.getSimpleName());
	}
	
	public Extractor save(Extractor extractor, String computationalModelSlug) throws ApiException {
		ComputationalModel parentComputationalModel = null;
		
		if (extractor.getSlug() == null || "".equals(extractor.getSlug())) {
			extractor.setSlug(KeyUtils.generate());
		}
		
		if (computationalModelSlug == null || "".equals(computationalModelSlug)) {
			throw new BadRequestApiException(String.format(Constants.MSG_ERROR.PARENT_ENTITY_NULL_ERROR, ComputationalModel.class.getName()));
		}
		
		try {
			parentComputationalModel = computationalModelService.findBySlug(computationalModelSlug);
		
		} catch (NotFoundApiException e) {
			throw new BadRequestApiException(
					String.format(Constants.MSG_ERROR.PARENT_ENTITY_NOT_FOUND_ERROR, ComputationalModel.class.getName(), 
							computationalModelSlug), e);
		}
		
		extractor.setComputationalModel(parentComputationalModel);
		
		return super.save(extractor);
	}
	
	@Transactional
	public Extractor update(Extractor extractor, String computationalModelSlug) throws ApiException {
		Extractor extractorDatabase = findBySlug(extractor.getSlug());
		extractor.setId(extractorDatabase.getId());
		
		ComputationalModel parentComputationalModel = null;
		
		if (computationalModelSlug == null || "".equals(computationalModelSlug)) {
			throw new BadRequestApiException(String.format(Constants.MSG_ERROR.PARENT_ENTITY_NULL_ERROR, ComputationalModel.class.getName()));
		}
		
		try {
			parentComputationalModel = computationalModelService.findBySlug(computationalModelSlug);
		
		} catch (NotFoundApiException e) {
			throw new BadRequestApiException(
					String.format(Constants.MSG_ERROR.PARENT_ENTITY_NOT_FOUND_ERROR, Experiment.class.getName(), 
							computationalModelSlug), e);
		}
		
		extractor.setComputationalModel(parentComputationalModel);
		
		return super.update(extractor);
	}
	
	public Extractor findBySlug(String slug, String authorization, String computationalModelSlug) throws ApiException {
		Extractor entity = findBySlug(slug);
		
		if (entity.getComputationalModel().getIsPublicData()) {
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
	
	@Override
	public Integer delete(String slug) throws ApiException {
		Extractor extractor = findBySlug(slug);
		Long totalRunning = extractorExecutionService
				.countByExtractorAndExecutionStatus(extractor, ExecutionStatus.RUNNING);

		if (totalRunning > 0) {
			throw new BadRequestApiException(Constants.MSG_ERROR.EXTRACTOR_CAN_NOT_BE_DELETED_ERROR);
		}
		
		if (extractor.getFileId() != null && !"".equals(extractor.getFileId())) {
			try {
				googleDriveService.deleteFileAsync(extractor.getFileId());
				
			} catch (NotFoundApiException e) {
				log.warn(Constants.MSG_WARN.GOOGLE_FILE_NOT_FOUND, extractor.getFileId());
			}
		}
		
		return super.delete(slug);
	}
	
	public Integer deleteByComputationalModel(ComputationalModel computationalModel) {
		List<Extractor> extractors = ExtractorRepository.findAllByComputationalModel(computationalModel);
		
		if (extractors == null || extractors.isEmpty()) {
			return 0;
		}

		Integer deletedResult = ExtractorRepository.deleteByComputationalModel(computationalModel);
		
		for (Extractor extractor : extractors) {
			if (extractor.getFileId() != null && !"".equals(extractor.getFileId())) {
				try {
					googleDriveService.deleteFileAsync(extractor.getFileId());
					
				} catch (NotFoundApiException e) {
					log.warn(Constants.MSG_WARN.GOOGLE_FILE_NOT_FOUND, extractor.getFileId());
					continue;
				}
			}
		}
		
		return deletedResult;
	}

	public Extractor uploadExtractor(String slug, MultipartFile extractorFile) throws ApiException, IOException {
		Extractor extractorDatabase = findBySlug(slug);
		
		if (extractorDatabase != null) {
			
			String parentFolderHash = new StringBuilder(
					StringParserUtils.replace(extractorDatabase.getComputationalModel().getName(), " ", "_"))
					.append("_")
					.append(extractorDatabase.getComputationalModel().getSlug()).toString();
			
			DriveFile driveFile = googleDriveService.uploadFile(
					parentFolderHash, FileUtils.multipartToFile(extractorFile, extractorFile.getOriginalFilename()), 
					UPLOAD.MODEL_EXTRACTOR_FOLDER, extractorDatabase.getComputationalModel().getIsPublicData());
			
			if (driveFile != null) {
				extractorDatabase.setFileId(driveFile.getFileId());
			}
			
			extractorDatabase.setFileContentType(extractorFile.getContentType());
			extractorDatabase.setFileName(extractorFile.getOriginalFilename());
		}
		
		return super.update(extractorDatabase);
	}

	public byte[] getExtractor(String fileId) throws NotFoundApiException {
		return googleDriveService.getFileBytesContent(fileId);
	}
	
}