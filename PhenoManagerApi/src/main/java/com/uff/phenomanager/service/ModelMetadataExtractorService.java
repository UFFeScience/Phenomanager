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
import com.uff.phenomanager.domain.ModelMetadataExtractor;
import com.uff.phenomanager.domain.api.google.DriveFile;
import com.uff.phenomanager.exception.ApiException;
import com.uff.phenomanager.exception.BadRequestApiException;
import com.uff.phenomanager.exception.NotFoundApiException;
import com.uff.phenomanager.exception.UnauthorizedApiException;
import com.uff.phenomanager.repository.ModelMetadataExtractorRepository;
import com.uff.phenomanager.service.api.google.GoogleDriveService;
import com.uff.phenomanager.service.core.ApiPermissionRestService;
import com.uff.phenomanager.util.FileUtils;
import com.uff.phenomanager.util.KeyUtils;
import com.uff.phenomanager.util.StringParserUtils;
import com.uff.phenomanager.util.TokenUtils;

@Service
public class ModelMetadataExtractorService extends ApiPermissionRestService<ModelMetadataExtractor, ModelMetadataExtractorRepository> {
	
	private static final Logger log = LoggerFactory.getLogger(ModelMetadataExtractorService.class);
	
	@Autowired
	private ModelMetadataExtractorRepository modelMetadataExtractorRepository;
	
	@Autowired
	private ComputationalModelService computationalModelService;
	
	@Lazy
	@Autowired
	private GoogleDriveService googleDriveService;
	
	@Override
	protected ModelMetadataExtractorRepository getRepository() {
		return modelMetadataExtractorRepository;
	}
	
	@Override
	protected Class<ModelMetadataExtractor> getEntityClass() {
		return ModelMetadataExtractor.class;
	}
	
	@Override
	protected String getPermissionEntityName() {
		return StringUtils.uncapitalize(ComputationalModel.class.getSimpleName());
	}
	
	public ModelMetadataExtractor save(ModelMetadataExtractor modelMetadataExtractor, String computationalModelSlug) throws ApiException {
		ComputationalModel parentComputationalModel = null;
		
		if (modelMetadataExtractor.getSlug() == null || "".equals(modelMetadataExtractor.getSlug())) {
			modelMetadataExtractor.setSlug(KeyUtils.generate());
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
		
		modelMetadataExtractor.setComputationalModel(parentComputationalModel);
		
		return super.save(modelMetadataExtractor);
	}
	
	@Transactional
	public ModelMetadataExtractor update(ModelMetadataExtractor modelMetadataExtractor, String computationalModelSlug) throws ApiException {
		ModelMetadataExtractor modelMetadataExtractorDatabase = findBySlug(modelMetadataExtractor.getSlug());
		modelMetadataExtractor.setId(modelMetadataExtractorDatabase.getId());
		
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
		
		modelMetadataExtractor.setComputationalModel(parentComputationalModel);
		
		return super.update(modelMetadataExtractor);
	}
	
	public ModelMetadataExtractor findBySlug(String slug, String authorization, String computationalModelSlug) throws ApiException {
		ModelMetadataExtractor entity = findBySlug(slug);
		
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
		ModelMetadataExtractor modelMetadataExtractor = findBySlug(slug);
		
		if (ExecutionStatus.RUNNING.equals(modelMetadataExtractor.getExecutionStatus())) {
			throw new BadRequestApiException(Constants.MSG_ERROR.EXTRACTOR_CAN_NOT_BE_DELETED_ERROR);
		}
		
		if (modelMetadataExtractor.getExtractorFileId() != null && !"".equals(modelMetadataExtractor.getExtractorFileId())) {
			try {
				googleDriveService.deleteFileAsync(modelMetadataExtractor.getExtractorFileId());
				
			} catch (NotFoundApiException e) {
				log.warn(Constants.MSG_WARN.GOOGLE_FILE_NOT_FOUND, modelMetadataExtractor.getExtractorFileId());
			}
		}
		
		return super.delete(slug);
	}
	
	public Integer deleteByComputationalModel(ComputationalModel computationalModel) {
		List<ModelMetadataExtractor> modelMetadataExtractors = modelMetadataExtractorRepository.findAllByComputationalModel(computationalModel);
		
		if (modelMetadataExtractors == null || modelMetadataExtractors.isEmpty()) {
			return 0;
		}

		Integer deletedResult = modelMetadataExtractorRepository.deleteByComputationalModel(computationalModel);
		
		for (ModelMetadataExtractor modelMetadataExtractor : modelMetadataExtractors) {
			if (modelMetadataExtractor.getExtractorFileId() != null && !"".equals(modelMetadataExtractor.getExtractorFileId())) {
				try {
					googleDriveService.deleteFileAsync(modelMetadataExtractor.getExtractorFileId());
					
				} catch (NotFoundApiException e) {
					log.warn(Constants.MSG_WARN.GOOGLE_FILE_NOT_FOUND, modelMetadataExtractor.getExtractorFileId());
					continue;
				}
			}
		}
		
		return deletedResult;
	}

	public ModelMetadataExtractor uploadExtractor(String slug, MultipartFile extractor) throws ApiException, IOException {
		ModelMetadataExtractor modelMetadataExtractor = findBySlug(slug);
		
		if (modelMetadataExtractor != null) {
			
			String parentFolderHash = new StringBuilder(
					StringParserUtils.replace(modelMetadataExtractor.getComputationalModel().getName(), " ", "_"))
					.append("_")
					.append(modelMetadataExtractor.getComputationalModel().getSlug()).toString();
			
			DriveFile driveFile = googleDriveService.uploadFile(
					parentFolderHash, FileUtils.multipartToFile(extractor, extractor.getOriginalFilename()), 
					UPLOAD.MODEL_EXTRACTOR_FOLDER, modelMetadataExtractor.getComputationalModel().getIsPublicData());
			
			if (driveFile != null) {
				modelMetadataExtractor.setExtractorFileId(driveFile.getFileId());
			}
			
			modelMetadataExtractor.setExtractorFileContentType(extractor.getContentType());
			modelMetadataExtractor.setExtractorFileName(extractor.getOriginalFilename());
		}
		
		return super.update(modelMetadataExtractor);
	}

	public byte[] getExtractor(String fileId) throws NotFoundApiException {
		return googleDriveService.getFileBytesContent(fileId);
	}
	
}