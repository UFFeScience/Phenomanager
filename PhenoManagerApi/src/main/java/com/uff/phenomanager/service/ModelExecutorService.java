package com.uff.phenomanager.service;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import com.uff.phenomanager.domain.ModelExecutor;
import com.uff.phenomanager.domain.api.google.DriveFile;
import com.uff.phenomanager.domain.core.filter.FilterOperator;
import com.uff.phenomanager.domain.core.filter.RequestFilter;
import com.uff.phenomanager.exception.ApiException;
import com.uff.phenomanager.exception.BadRequestApiException;
import com.uff.phenomanager.exception.NotFoundApiException;
import com.uff.phenomanager.exception.UnauthorizedApiException;
import com.uff.phenomanager.repository.ModelExecutorRepository;
import com.uff.phenomanager.service.api.google.GoogleDriveService;
import com.uff.phenomanager.service.core.ApiPermissionRestService;
import com.uff.phenomanager.util.FileUtils;
import com.uff.phenomanager.util.KeyUtils;
import com.uff.phenomanager.util.StringParserUtils;
import com.uff.phenomanager.util.TokenUtils;

@Service
public class ModelExecutorService extends ApiPermissionRestService<ModelExecutor, ModelExecutorRepository> {
	
	private static final Logger log = LoggerFactory.getLogger(ModelExecutorService.class);
	
	@Autowired
	private ModelExecutorRepository modelExecutorRepository;
	
	@Autowired
	private ComputationalModelService computationalModelService;
	
	@Lazy
	@Autowired
	private GoogleDriveService googleDriveService;
	
	@Override
	protected ModelExecutorRepository getRepository() {
		return modelExecutorRepository;
	}
	
	@Override
	protected Class<ModelExecutor> getEntityClass() {
		return ModelExecutor.class;
	}
	
	@Override
	protected String getPermissionEntityName() {
		return StringUtils.uncapitalize(ComputationalModel.class.getSimpleName());
	}
	
	public ModelExecutor save(ModelExecutor modelExecutor, String computationalModelSlug) throws ApiException {
		ComputationalModel parentComputationalModel = null;
		
		if (modelExecutor.getSlug() == null || "".equals(modelExecutor.getSlug())) {
			modelExecutor.setSlug(KeyUtils.generate());
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
		
		modelExecutor.setComputationalModel(parentComputationalModel);
		
		ModelExecutor currentModelExecutorActive = findByComputationalModelAndActive(parentComputationalModel, Boolean.TRUE);
		
		if (currentModelExecutorActive != null && 
				!currentModelExecutorActive.getExecutionStatus().equals(ExecutionStatus.RUNNING)) {
			
			currentModelExecutorActive.setActive(Boolean.FALSE);
			super.update(currentModelExecutorActive);
		
		} else if (currentModelExecutorActive != null && 
				!currentModelExecutorActive.getExecutionStatus().equals(ExecutionStatus.RUNNING)) {
			
			modelExecutor.setActive(Boolean.FALSE);
		}
		
		return super.save(modelExecutor);
   }
	
	public ModelExecutor update(ModelExecutor modelExecutor, String computationalModelSlug) throws ApiException {
		ModelExecutor modelExecutorDatabase = findBySlug(modelExecutor.getSlug());
		modelExecutor.setId(modelExecutorDatabase.getId());
		
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
		
		modelExecutor.setComputationalModel(parentComputationalModel);
		
		if (modelExecutor.getActive()) {
			ModelExecutor currentModelExecutorActive = findByComputationalModelAndActive(parentComputationalModel, Boolean.TRUE);
			
			if (currentModelExecutorActive != null && 
					!currentModelExecutorActive.getSlug().equals(modelExecutor.getSlug()) &&
					!currentModelExecutorActive.getExecutionStatus().equals(ExecutionStatus.RUNNING)) {
				
				currentModelExecutorActive.setActive(Boolean.FALSE);
				super.update(currentModelExecutorActive);
			
			} else {
				modelExecutor.setActive(Boolean.FALSE);
			}
		}
		
		return super.update(modelExecutor);
	}
	
	public ModelExecutor findBySlug(String slug, String authorization) throws ApiException {
		ModelExecutor entity = findBySlug(slug);
		
		if (entity.getComputationalModel().getIsPublicData()) {
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
	
	public ModelExecutor findByComputationalModelAndActive(ComputationalModel computationalModel, Boolean active) {
		return modelExecutorRepository.findByComputationalModelAndActive(computationalModel, active);
	}
	
	public List<ModelExecutor> findAllByComputationalModelAndExecutionStatus(ComputationalModel computationalModel, 
			ExecutionStatus executionStatus) {
		
		return modelExecutorRepository.findAllByComputationalModelAndExecutionStatus(computationalModel, executionStatus);
	}
	
	@Override
	public Integer delete(String slug) throws NotFoundApiException {
		ModelExecutor modelExecutor = findBySlug(slug);
		
		if (modelExecutor.getExecutorFileId() != null && !"".equals(modelExecutor.getExecutorFileId())) {
			try {
				googleDriveService.deleteFileAsync(modelExecutor.getExecutorFileId());
				
			} catch (NotFoundApiException e) {
				log.warn(Constants.MSG_WARN.GOOGLE_FILE_NOT_FOUND, modelExecutor.getExecutorFileId());
			}
		}
		
		return super.delete(slug);
	}

	public Integer deleteByComputationalModel(ComputationalModel computationalModel) {
		List<ModelExecutor> modelExecutors = modelExecutorRepository.findAllByComputationalModel(computationalModel);
		
		if (modelExecutors == null || modelExecutors.isEmpty()) {
			return 0;
		}

		Integer deletedResult = modelExecutorRepository.deleteByComputationalModel(computationalModel);
		
		for (ModelExecutor modelExecutor : modelExecutors) {
			if (modelExecutor.getExecutorFileId() != null && !"".equals(modelExecutor.getExecutorFileId())) {
				try {
					googleDriveService.deleteFileAsync(modelExecutor.getExecutorFileId());
					
				} catch (NotFoundApiException e) {
					log.warn(Constants.MSG_WARN.GOOGLE_FILE_NOT_FOUND, modelExecutor.getExecutorFileId());
					continue;
				}
			}
		}
		
		return deletedResult;
	}

	public Map<String, Long> countAllRunningModels(String authorization) throws ApiException {
		RequestFilter runningFilter = new RequestFilter();
		runningFilter.addAndFilter("executionStatus", ExecutionStatus.RUNNING, FilterOperator.EQ);
		
		Map<String, Long> totalRunningModels = new HashMap<>();
		totalRunningModels.put("totalRunningModels", countAll(runningFilter, authorization));
		
		return totalRunningModels;
	}

	public Map<String, Long> countAllErrorModels(String authorization) throws ApiException {
		RequestFilter errorFilter = new RequestFilter();
		errorFilter.addAndFilter("executionStatus", ExecutionStatus.FAILURE, FilterOperator.EQ);
		
		Map<String, Long> totalErrorModels = new HashMap<>();
		totalErrorModels.put("totalErrorModels", countAll(errorFilter, authorization));
		
		return totalErrorModels;
	}

	public ModelExecutor uploadExecutor(String slug, MultipartFile executor) throws ApiException, IOException {
		ModelExecutor modelExecutorDatabase = findBySlug(slug);
		
		if (modelExecutorDatabase != null) {
			
			String parentFolderHash = new StringBuilder(
					StringParserUtils.replace(modelExecutorDatabase.getComputationalModel().getName(), " ", "_"))
					.append("_")
					.append(modelExecutorDatabase.getComputationalModel().getSlug()).toString();
			
			DriveFile driveFile = googleDriveService.uploadFile(
					parentFolderHash, FileUtils.multipartToFile(executor, executor.getOriginalFilename()), 
					UPLOAD.MODEL_EXECUTOR_FOLDER, modelExecutorDatabase.getComputationalModel().getIsPublicData());

			if (driveFile != null) {
				modelExecutorDatabase.setExecutorFileId(driveFile.getFileId());
			}
			
			modelExecutorDatabase.setExecutorFileContentType(executor.getContentType());
			modelExecutorDatabase.setExecutorFileName(executor.getOriginalFilename());
		}
		
		return super.update(modelExecutorDatabase);
	}

	public byte[] getExecutor(String fileId) throws NotFoundApiException {
		return googleDriveService.getFileBytesContent(fileId);
	}
	
}