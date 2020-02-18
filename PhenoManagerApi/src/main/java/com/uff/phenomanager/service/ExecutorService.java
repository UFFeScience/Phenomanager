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
import com.uff.phenomanager.domain.ExecutionStatus;
import com.uff.phenomanager.domain.Experiment;
import com.uff.phenomanager.domain.Executor;
import com.uff.phenomanager.domain.api.google.DriveFile;
import com.uff.phenomanager.exception.ApiException;
import com.uff.phenomanager.exception.BadRequestApiException;
import com.uff.phenomanager.exception.NotFoundApiException;
import com.uff.phenomanager.exception.UnauthorizedApiException;
import com.uff.phenomanager.repository.ExecutorRepository;
import com.uff.phenomanager.service.api.google.GoogleDriveService;
import com.uff.phenomanager.service.core.ApiPermissionRestService;
import com.uff.phenomanager.util.FileUtils;
import com.uff.phenomanager.util.KeyUtils;
import com.uff.phenomanager.util.StringParserUtils;
import com.uff.phenomanager.util.TokenUtils;

@Service
public class ExecutorService extends ApiPermissionRestService<Executor, ExecutorRepository> {
	
	private static final Logger log = LoggerFactory.getLogger(ExecutorService.class);
	
	@Autowired
	private ExecutorRepository executorRepository;
	
	@Autowired
	private ComputationalModelService computationalModelService;
	
	@Autowired
	private ExecutionService executionService;
	
	@Lazy
	@Autowired
	private GoogleDriveService googleDriveService;
	
	@Override
	protected ExecutorRepository getRepository() {
		return executorRepository;
	}
	
	@Override
	protected Class<Executor> getEntityClass() {
		return Executor.class;
	}
	
	@Override
	protected String getPermissionEntityName() {
		return StringUtils.uncapitalize(ComputationalModel.class.getSimpleName());
	}
	
	public Executor save(Executor executor, String computationalModelSlug) throws ApiException {
		ComputationalModel parentComputationalModel = null;
		
		if (executor.getSlug() == null || "".equals(executor.getSlug())) {
			executor.setSlug(KeyUtils.generate());
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
		
		executor.setComputationalModel(parentComputationalModel);
		
		return super.save(executor);
   }
	
	public Executor update(Executor executor, String computationalModelSlug) throws ApiException {
		Executor executorDatabase = findBySlug(executor.getSlug());
		executor.setId(executorDatabase.getId());
		
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
		
		executor.setComputationalModel(parentComputationalModel);
		
		return super.update(executor);
	}
	
	public Executor findBySlug(String slug, String authorization, String computationalModelSlug) throws ApiException {
		Executor entity = findBySlug(slug);
		
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
		Executor executor = findBySlug(slug);
		Long totalRunning = executionService.countByExecutorAndStatus(executor, ExecutionStatus.RUNNING);
		
		if (totalRunning > 0) {
			throw new BadRequestApiException(Constants.MSG_ERROR.EXECUTOR_CAN_NOT_BE_DELETED_ERROR);
		}
		
		if (executor.getFileId() != null && !"".equals(executor.getFileId())) {
			try {
				googleDriveService.deleteFileAsync(executor.getFileId());
				
			} catch (NotFoundApiException e) {
				log.warn(Constants.MSG_WARN.GOOGLE_FILE_NOT_FOUND, executor.getFileId());
			}
		}
		
		return super.delete(slug);
	}

	public Integer deleteByComputationalModel(ComputationalModel computationalModel) {
		List<Executor> executors = executorRepository.findAllByComputationalModel(computationalModel);
		
		if (executors == null || executors.isEmpty()) {
			return 0;
		}

		Integer deletedResult = executorRepository.deleteByComputationalModel(computationalModel);
		
		for (Executor executor : executors) {
			if (executor.getFileId() != null && !"".equals(executor.getFileId())) {
				try {
					googleDriveService.deleteFileAsync(executor.getFileId());
					
				} catch (NotFoundApiException e) {
					log.warn(Constants.MSG_WARN.GOOGLE_FILE_NOT_FOUND, executor.getFileId());
					continue;
				}
			}
		}
		
		return deletedResult;
	}

	public Executor uploadExecutor(String slug, MultipartFile executorFile) throws ApiException, IOException {
		Executor executorDatabase = findBySlug(slug);
		
		if (executorDatabase != null) {
			
			String parentFolderHash = new StringBuilder(
					StringParserUtils.replace(executorDatabase.getComputationalModel().getName(), " ", "_"))
					.append("_")
					.append(executorDatabase.getComputationalModel().getSlug()).toString();
			
			DriveFile driveFile = googleDriveService.uploadFile(
					parentFolderHash, FileUtils.multipartToFile(executorFile, executorFile.getOriginalFilename()), 
					UPLOAD.MODEL_EXECUTOR_FOLDER, executorDatabase.getComputationalModel().getIsPublicData());

			if (driveFile != null) {
				executorDatabase.setFileId(driveFile.getFileId());
			}
			
			executorDatabase.setFileContentType(executorFile.getContentType());
			executorDatabase.setFileName(executorFile.getOriginalFilename());
		}
		
		return super.update(executorDatabase);
	}

	public byte[] getExecutor(String fileId) throws NotFoundApiException {
		return googleDriveService.getFileBytesContent(fileId);
	}
	
}