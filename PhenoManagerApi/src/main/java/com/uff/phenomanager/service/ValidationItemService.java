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
import com.uff.phenomanager.Constants.UPLOAD;
import com.uff.phenomanager.domain.Experiment;
import com.uff.phenomanager.domain.ValidationItem;
import com.uff.phenomanager.domain.api.google.DriveFile;
import com.uff.phenomanager.domain.core.filter.FilterOperator;
import com.uff.phenomanager.domain.core.filter.RequestFilter;
import com.uff.phenomanager.domain.dto.ValidationStatisticsDto;
import com.uff.phenomanager.exception.ApiException;
import com.uff.phenomanager.exception.BadRequestApiException;
import com.uff.phenomanager.exception.NotFoundApiException;
import com.uff.phenomanager.repository.ValidationItemRepository;
import com.uff.phenomanager.service.api.google.GoogleDriveService;
import com.uff.phenomanager.service.core.ApiPermissionRestService;
import com.uff.phenomanager.util.FileUtils;
import com.uff.phenomanager.util.KeyUtils;
import com.uff.phenomanager.util.StringParserUtils;

@Service
public class ValidationItemService extends ApiPermissionRestService<ValidationItem, ValidationItemRepository> {
	
	private static final Logger log = LoggerFactory.getLogger(ValidationItemService.class);
	
	@Autowired
	private ValidationItemRepository validationItemRepository;
	
	@Autowired
	private ExperimentService experimentService;
	
	@Lazy
	@Autowired
	private GoogleDriveService googleDriveService;
	
	@Override
	protected ValidationItemRepository getRepository() {
		return validationItemRepository;
	}
	
	@Override
	protected Class<ValidationItem> getEntityClass() {
		return ValidationItem.class;
	}
	
	@Override
	protected String getPermissionEntityName() {
		return StringUtils.uncapitalize(Experiment.class.getSimpleName());
	}
	
	public ValidationItem save(ValidationItem validationItem, String experimentSlug) throws ApiException {
		Experiment parentExperiment = null;
		
		if (validationItem.getSlug() == null || "".equals(validationItem.getSlug())) {
			validationItem.setSlug(KeyUtils.generate());
		}
		
		if (experimentSlug == null || "".equals(experimentSlug)) {
			throw new BadRequestApiException(String.format(Constants.MSG_ERROR.PARENT_ENTITY_NULL_ERROR, Experiment.class.getName()));
		}
		
		try {
			parentExperiment = experimentService.findBySlug(experimentSlug);
		} catch (NotFoundApiException e) {
			throw new BadRequestApiException(
					String.format(Constants.MSG_ERROR.PARENT_ENTITY_NOT_FOUND_ERROR, Experiment.class.getName(), 
							experimentSlug), e);
		}
		
		validationItem.setExperiment(parentExperiment);
		
		return super.save(validationItem);
	}
	
	@Transactional
	public ValidationItem update(ValidationItem validationItem, String experimentSlug) throws ApiException {
		ValidationItem validationItemDatabase = findBySlug(validationItem.getSlug());
		validationItem.setId(validationItemDatabase.getId());
		
		Experiment parentExperiment = null;
		
		if (experimentSlug == null || "".equals(experimentSlug)) {
			throw new BadRequestApiException(String.format(Constants.MSG_ERROR.PARENT_ENTITY_NULL_ERROR, Experiment.class.getName()));
		}
		
		try {
			parentExperiment = experimentService.findBySlug(experimentSlug);
		} catch (NotFoundApiException e) {
			throw new BadRequestApiException(
					String.format(Constants.MSG_ERROR.PARENT_ENTITY_NOT_FOUND_ERROR, Experiment.class.getName(), 
							experimentSlug), e);
		}
		
		validationItem.setExperiment(parentExperiment);		
		
		return super.update(validationItem);
	}
	
	public ValidationItem uploadValidationEvidence(String slug, MultipartFile validationEvidence) throws ApiException, IOException {
		ValidationItem validationItemDatabase = findBySlug(slug);
		validationItemDatabase.setValidated(Boolean.TRUE);
		
		if (validationEvidence != null) {
			String parentFolderHash = new StringBuilder(
					StringParserUtils.replace(validationItemDatabase.getExperiment().getName(), " ", "_"))
					.append("_")
					.append(validationItemDatabase.getExperiment().getSlug()).toString();
			
			DriveFile driveFile = googleDriveService.uploadFile(
					parentFolderHash, FileUtils.multipartToFile(validationEvidence, validationEvidence.getOriginalFilename()), 
					UPLOAD.VALIDATION_ITEM_FOLDER, Boolean.FALSE);
			
			if (driveFile != null) {
				validationItemDatabase.setValidationEvidenceFileId(driveFile.getFileId());
			}
			
			validationItemDatabase.setValidationEvidenceFileContentType(validationEvidence.getContentType());
			validationItemDatabase.setValidationEvidenceFileName(validationEvidence.getOriginalFilename());
		}
		
		return super.update(validationItemDatabase);
	}

	public ValidationStatisticsDto getStatistics(String authorization) throws ApiException {
		RequestFilter validatedFilter = new RequestFilter();
		validatedFilter.addAndFilter("validated", Boolean.TRUE, FilterOperator.EQ);
		
		return ValidationStatisticsDto.builder()
				.itemsValidated(countAll(validatedFilter, authorization))
				.totalItems(countAll(new RequestFilter(), authorization))
				.build();
	}
	
	@Override
	public Integer delete(String slug) throws ApiException {
		ValidationItem validationItem = findBySlug(slug);
		
		if (validationItem.getValidationEvidenceFileId() != null && !"".equals(validationItem.getValidationEvidenceFileId())) {
			try {
				googleDriveService.deleteFileAsync(validationItem.getValidationEvidenceFileId());
				
			} catch (NotFoundApiException e) {
				log.warn(Constants.MSG_WARN.GOOGLE_FILE_NOT_FOUND, validationItem.getValidationEvidenceFileId());
			}
		}
		
		return super.delete(slug);
	}

	public Integer deleteByExperiment(Experiment experiment) {
		List<ValidationItem> validationItems = validationItemRepository.findAllByExperiment(experiment);
		
		if (validationItems == null || validationItems.isEmpty()) {
			return 0;
		}

		Integer deletedResult = validationItemRepository.deleteByExperiment(experiment);
		
		for (ValidationItem validationItem : validationItems) {
			if (validationItem.getValidationEvidenceFileId() != null && !"".equals(validationItem.getValidationEvidenceFileId())) {
				try {
					googleDriveService.deleteFileAsync(validationItem.getValidationEvidenceFileId());
					
				} catch (NotFoundApiException e) {
					log.warn(Constants.MSG_WARN.GOOGLE_FILE_NOT_FOUND, validationItem.getValidationEvidenceFileId());
					continue;
				}
			}
		}
		
		return deletedResult;
	}

	public byte[] getValidationEvidence(String fileId) throws NotFoundApiException {
		return googleDriveService.getFileBytesContent(fileId);
	}
	
}