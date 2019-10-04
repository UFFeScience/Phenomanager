package com.uff.phenomanager.service;

import java.io.IOException;
import java.util.List;

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
import com.uff.phenomanager.domain.ConceptualParam;
import com.uff.phenomanager.domain.Experiment;
import com.uff.phenomanager.domain.InstanceParam;
import com.uff.phenomanager.domain.api.google.DriveFile;
import com.uff.phenomanager.exception.ApiException;
import com.uff.phenomanager.exception.BadRequestApiException;
import com.uff.phenomanager.exception.NotFoundApiException;
import com.uff.phenomanager.exception.UnauthorizedApiException;
import com.uff.phenomanager.repository.InstanceParamRepository;
import com.uff.phenomanager.service.api.google.GoogleDriveService;
import com.uff.phenomanager.service.core.ApiPermissionRestService;
import com.uff.phenomanager.util.FileUtils;
import com.uff.phenomanager.util.KeyUtils;
import com.uff.phenomanager.util.StringParserUtils;
import com.uff.phenomanager.util.TokenUtils;

@Service
public class InstanceParamService extends ApiPermissionRestService<InstanceParam, InstanceParamRepository> {
	
	private static final Logger log = LoggerFactory.getLogger(InstanceParamService.class);
	
	@Autowired
	private InstanceParamRepository instanceParamRepository;
	
	@Autowired
	private ConceptualParamService conceptualParamService;
	
	@Autowired
	private ComputationalModelService computationalModelService;
	
	@Lazy
	@Autowired
	private GoogleDriveService googleDriveService;
	
	@Override
	protected InstanceParamRepository getRepository() {
		return instanceParamRepository;
	}
	
	@Override
	protected Class<InstanceParam> getEntityClass() {
		return InstanceParam.class;
	}
	
	@Override
	protected String getPermissionEntityName() {
		return StringUtils.uncapitalize(ComputationalModel.class.getSimpleName());
	}
	
	public InstanceParam save(InstanceParam instanceParam, String computationalModelSlug) throws ApiException {
		ComputationalModel parentComputationalModel = null;
		ConceptualParam parentConceptualParam = null;
		
		if (instanceParam.getSlug() == null || "".equals(instanceParam.getSlug())) {
			instanceParam.setSlug(KeyUtils.generate());
		}
		
		if (computationalModelSlug == null || "".equals(computationalModelSlug)) {
			throw new BadRequestApiException(String.format(Constants.MSG_ERROR.PARENT_ENTITY_NULL_ERROR, ComputationalModel.class.getName()));
		}
		
		if (instanceParam.getConceptualParam() == null || instanceParam.getConceptualParam().getSlug() == null || 
				"".equals(instanceParam.getConceptualParam().getSlug())) {
			throw new BadRequestApiException(String.format(Constants.MSG_ERROR.PARENT_ENTITY_NULL_ERROR, ConceptualParam.class.getName()));
		}
		
		try {
			parentComputationalModel = computationalModelService.findBySlug(computationalModelSlug);
		} catch (NotFoundApiException e) {
			throw new BadRequestApiException(
					String.format(Constants.MSG_ERROR.PARENT_ENTITY_NOT_FOUND_ERROR, ComputationalModel.class.getName(), 
							computationalModelSlug), e);
		}
		
		try {
			parentConceptualParam = conceptualParamService.findBySlug(instanceParam.getConceptualParam().getSlug());
		} catch (NotFoundApiException e) {
			throw new BadRequestApiException(
					String.format(Constants.MSG_ERROR.PARENT_ENTITY_NOT_FOUND_ERROR, ComputationalModel.class.getName(), 
							computationalModelSlug), e);
		}
		
		instanceParam.setComputationalModel(parentComputationalModel);
		instanceParam.setConceptualParam(parentConceptualParam);
		
		return super.save(instanceParam);
   }
	
	public InstanceParam update(InstanceParam instanceParam, String computationalModelSlug) throws ApiException {
		InstanceParam instanceParamDatabase = findBySlug(instanceParam.getSlug());
		instanceParam.setId(instanceParamDatabase.getId());
		
		ComputationalModel parentComputationalModel = null;
		ConceptualParam parentConceptualParam = null;
		
		if (computationalModelSlug == null || "".equals(computationalModelSlug)) {
			throw new BadRequestApiException(String.format(Constants.MSG_ERROR.PARENT_ENTITY_NULL_ERROR, ComputationalModel.class.getName()));
		}
		
		if (instanceParam.getConceptualParam() == null || instanceParam.getConceptualParam().getSlug() == null || 
				"".equals(instanceParam.getConceptualParam().getSlug())) {
			throw new BadRequestApiException(String.format(Constants.MSG_ERROR.PARENT_ENTITY_NULL_ERROR, ConceptualParam.class.getName()));
		}
		
		try {
			parentComputationalModel = computationalModelService.findBySlug(computationalModelSlug);
		} catch (NotFoundApiException e) {
			throw new BadRequestApiException(
					String.format(Constants.MSG_ERROR.PARENT_ENTITY_NOT_FOUND_ERROR, Experiment.class.getName(), 
							computationalModelSlug), e);
		}
		
		try {
			parentConceptualParam = conceptualParamService.findBySlug(instanceParam.getConceptualParam().getSlug());
		} catch (NotFoundApiException e) {
			throw new BadRequestApiException(
					String.format(Constants.MSG_ERROR.PARENT_ENTITY_NOT_FOUND_ERROR, ComputationalModel.class.getName(), 
							computationalModelSlug), e);
		}
		
		instanceParam.setComputationalModel(parentComputationalModel);
		instanceParam.setConceptualParam(parentConceptualParam);		
		
		return super.update(instanceParam);
	}
	
	public InstanceParam findBySlug(String slug, String authorization, String computationalModelSlug) throws ApiException {
		InstanceParam entity = findBySlug(slug);
		
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
	
	public List<InstanceParam> findAllByComputationalModel(ComputationalModel computationalModel) {
		return instanceParamRepository.findAllByComputationalModel(computationalModel);
	}
	
	@Override
	public Integer delete(String slug) throws ApiException {
		InstanceParam instanceParam = findBySlug(slug);
		
		if (instanceParam.getValueFileId() != null && !"".equals(instanceParam.getValueFileId())) {
			try {
				googleDriveService.deleteFileAsync(instanceParam.getValueFileId());
				
			} catch (NotFoundApiException e) {
				log.warn(Constants.MSG_WARN.GOOGLE_FILE_NOT_FOUND, instanceParam.getValueFileId());
			}
		}
		
		return super.delete(slug);
	}

	public Integer deleteByComputationalModel(ComputationalModel computationalModel) {
		List<InstanceParam> instanceParams = findAllByComputationalModel(computationalModel);
		
		if (instanceParams == null || instanceParams.isEmpty()) {
			return 0;
		}

		Integer deletedResult = instanceParamRepository.deleteByComputationalModel(computationalModel);
		
		for (InstanceParam instanceParam : instanceParams) {
			if (instanceParam.getValueFileId() != null && !"".equals(instanceParam.getValueFileId())) {
				try {
					googleDriveService.deleteFileAsync(instanceParam.getValueFileId());
					
				} catch (NotFoundApiException e) {
					log.warn(Constants.MSG_WARN.GOOGLE_FILE_NOT_FOUND, instanceParam.getValueFileId());
					continue;
				}
			}
		}
		
		return deletedResult;
	}
	
	public Integer deleteByConceptualParam(ConceptualParam conceptualParam) {
		List<InstanceParam> instanceParams = instanceParamRepository.findAllByConceptualParam(conceptualParam);
		
		if (instanceParams == null || instanceParams.isEmpty()) {
			return 0;
		}

		Integer deletedResult = instanceParamRepository.deleteByConceptualParam(conceptualParam);
		
		for (InstanceParam instanceParam : instanceParams) {
			if (instanceParam.getValueFileId() != null && !"".equals(instanceParam.getValueFileId())) {
				try {
					googleDriveService.deleteFileAsync(instanceParam.getValueFileId());
					
				} catch (NotFoundApiException e) {
					log.warn(Constants.MSG_WARN.GOOGLE_FILE_NOT_FOUND, instanceParam.getValueFileId());
					continue;
				}
			}
		}
		
		return deletedResult;
	}

	public InstanceParam uploadValueFile(String slug, MultipartFile valueFile) throws ApiException, IOException {
		InstanceParam instanceParamDatabase = findBySlug(slug);

		
		if (instanceParamDatabase != null) {
			
			String parentFolderHash = new StringBuilder(
					StringParserUtils.replace(instanceParamDatabase.getComputationalModel().getName(), " ", "_"))
					.append("_")
					.append(instanceParamDatabase.getComputationalModel().getSlug()).toString();
			
			DriveFile driveFile = googleDriveService.uploadFile(
					parentFolderHash, FileUtils.multipartToFile(valueFile, valueFile.getOriginalFilename()), 
					UPLOAD.INSTANCE_PARAM_FOLDER, instanceParamDatabase.getComputationalModel().getIsPublicData());
			
			if (driveFile != null) {
				instanceParamDatabase.setValueFileId(driveFile.getFileId());
			}
			
			instanceParamDatabase.setValueFileContentType(valueFile.getContentType());
			instanceParamDatabase.setValueFileName(valueFile.getOriginalFilename());
		}
		
		return super.update(instanceParamDatabase);
	}

	public byte[] getValueFile(String fileId) throws ApiException {
		return googleDriveService.getFileBytesContent(fileId);
	}
	
}