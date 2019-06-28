package com.uff.phenomanager.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.uff.phenomanager.Constants;
import com.uff.phenomanager.Constants.CONTROLLER;
import com.uff.phenomanager.Constants.CONTROLLER.COMPUTATIONAL_MODEL;
import com.uff.phenomanager.Constants.CONTROLLER.EXTRACTOR_METADATA;
import com.uff.phenomanager.Constants.CONTROLLER.INSTANCE_PARAM;
import com.uff.phenomanager.Constants.CONTROLLER.MODEL_EXECUTOR;
import com.uff.phenomanager.Constants.CONTROLLER.MODEL_RESULT_METADATA;
import com.uff.phenomanager.Constants.MSG_ERROR;
import com.uff.phenomanager.config.security.TokenAuthenticationService;
import com.uff.phenomanager.domain.ComputationalModel;
import com.uff.phenomanager.domain.ExtractorMetadata;
import com.uff.phenomanager.domain.InstanceParam;
import com.uff.phenomanager.domain.ModelResultMetadata;
import com.uff.phenomanager.domain.Permission;
import com.uff.phenomanager.domain.User;
import com.uff.phenomanager.exception.ApiException;
import com.uff.phenomanager.exception.NotFoundApiException;
import com.uff.phenomanager.exception.UnauthorizedApiException;
import com.uff.phenomanager.repository.ModelResultMetadataRepository;
import com.uff.phenomanager.service.api.google.GoogleDriveService;
import com.uff.phenomanager.service.core.ApiPermissionRestService;
import com.uff.phenomanager.util.TokenUtils;

@Service
public class ModelResultMetadataService extends ApiPermissionRestService<ModelResultMetadata, ModelResultMetadataRepository> {

	private static final Logger log = LoggerFactory.getLogger(ModelResultMetadataService.class);
	
	@Autowired
	private ModelResultMetadataRepository modelResultMetadataRepository;
	
	@Autowired
	private ComputationalModelService computationalModelService;
	
	@Autowired
	private InstanceParamService instanceParamService;
	
	@Autowired
	private ExtractorMetadataService extractorMetadataService;
	
	@Autowired
	private TokenAuthenticationService tokenAuthenticationService;
	
	@Lazy
	@Autowired
	private GoogleDriveService googleDriveService;
	
	@Value(Constants.BASE_DOMAIN_URL)
	private String baseDomainUrl;
	
	@Override
	protected ModelResultMetadataRepository getRepository() {
		return modelResultMetadataRepository;
	}
	
	@Override
	protected Class<ModelResultMetadata> getEntityClass() {
		return ModelResultMetadata.class;
	}
	
	@Override
	protected String getPermissionEntityName() {
		return StringUtils.uncapitalize(ComputationalModel.class.getSimpleName());
	}
	
	public ModelResultMetadata findBySlug(String slug, String authorization, String computationalModelSlug) throws ApiException {
		ModelResultMetadata entity = findBySlug(slug);
		ComputationalModel computationalModel = computationalModelService.findBySlug(computationalModelSlug);
		
		if (computationalModel.getIsPublicData()) {
			return entity;
		}
		Boolean hasAuthorization = Boolean.TRUE;
		
        if (authorization == null || "".equals(authorization)) {
        	hasAuthorization = Boolean.FALSE;
		
        } else if (!tokenAuthenticationService.validateToken(authorization)) {
			hasAuthorization = Boolean.FALSE;
		
        } else if (!allowPermissionReadAccess(authorization, slug)) {
        	hasAuthorization = Boolean.FALSE;
        }
        
		if (!hasAuthorization) {
	       throw new UnauthorizedApiException(MSG_ERROR.AUTHORIZATION_TOKEN_NOT_VALID);
	    }
		
		return entity;
	}
	
	public Map<String, Object> getResearchObject(String slug, String authorization, String computationalModelSlug) throws ApiException {
		ComputationalModel computationalModel = computationalModelService.findBySlug(computationalModelSlug);
		
		if (computationalModel.getIsPublicData()) {
			return generateResearchObjectJson(slug, authorization, computationalModel);
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
		
		return generateResearchObjectJson(slug, authorization, computationalModel);
	}

	private Map<String, Object> generateResearchObjectJson(String slug, String authorization, ComputationalModel computationalModel) throws ApiException {
		Map<String, Object> researchObject = new LinkedHashMap<>();		
		
		researchObject.put("@context", buildContext());
		researchObject.put("@graph", buildGraph(slug, authorization, computationalModel));
		
		return researchObject;
	}

	private Map<String, Object> buildContext() {
		Map<String, Object> context = new HashMap<>();
		context.put("ro", "http://purl.org/wf4ever/ro#");
		context.put("prov", "http://www.w3.org/ns/prov#");
		context.put("dc", "http://purl.org/dc/terms/");
		context.put("foaf", "http://xmlns.com/foaf/0.1/");
		context.put("xhv", "http://www.w3.org/1999/xhtml/vocab#");
		context.put("ore", "http://www.openarchives.org/ore/terms/");
		context.put("schema", "http://schema.org/");
		
		Map<String, Object> type = new HashMap<>();
		type.put("@type", "@id");
		
		context.put("prov:wasAttributedTo", type);
		context.put("ore:aggregates", type);
		context.put("dc:contributor", type);
		context.put("dc:creator", type);
		context.put("xhv:license", type);
		context.put("prov:wasRevisionOf", type);
		context.put("schema:contributor", type);
		context.put("ore:isDescribedBy", type);
		
		return context;
	}
	
	private List<Object> buildGraph(String slug, String authorization, ComputationalModel computationalModel) throws ApiException {
		ModelResultMetadata modelResultMetadata = findBySlug(slug, authorization, computationalModel.getSlug());
		
		List<Object> graphElements = new ArrayList<>();
		
		Map<String, Object> graphElement = new HashMap<>();
		graphElement.put("@id", computationalModel.getSlug());
		
		List<String> type = new ArrayList<>();
		type.add("ro:ResearchObject");
		type.add("ore:Aggregation");
		graphElement.put("@type", type);
		graphElements.add(graphElement);
		
		graphElement = new HashMap<>();
		graphElement.put("schema:name", computationalModel.getName());
		graphElements.add(graphElement);
		
		graphElement = new HashMap<>();
		graphElement.put("dc:creator", modelResultMetadata.getUserAgent().getSlug());
		graphElements.add(graphElement);
		
		graphElement = new HashMap<>();
		graphElement.put("dc:abstract", computationalModel.getDescription());
		graphElements.add(graphElement);
		
		graphElement = new HashMap<>();
		
		Set<String> contributorSlugs = buildContributorSlugs(computationalModel, graphElement);
		
		graphElements.add(graphElement);
		
		graphElement = new HashMap<>();
		graphElement.put("dc:title", computationalModel.getName());
		graphElements.add(graphElement);
		
		graphElement = new HashMap<>();
		graphElement.put("ore:aggregates", buildAggregates(computationalModel, modelResultMetadata));
		graphElements.add(graphElement);
		
		graphElement = new HashMap<>();
		graphElement.put("prov:wasAttributedTo", contributorSlugs);
		graphElements.add(graphElement);
		
		graphElement = new HashMap<>();
		graphElement.put("schema:contributor", contributorSlugs);
		graphElements.add(graphElement);
		
		buildContributors(computationalModel, graphElements);
		
		return graphElements;
	}

	private void buildContributors(ComputationalModel computationalModel, List<Object> graphElements) {
		Map<String, Object> graphElement;
		if (computationalModel.getPermissions() != null && 
				!computationalModel.getPermissions().isEmpty()) {
			
			for (Permission permission : computationalModel.getPermissions()) {
				if (permission.getUser() != null) {
					
					graphElement = new HashMap<>();
					graphElement.put("@id", permission.getUser().getSlug());
					graphElement.put("@@type", "foaf:Person");
					graphElement.put("schema:name", permission.getUser().getName());
					graphElement.put("foaf:name", permission.getUser().getName());
					graphElements.add(graphElement);
				
				} else if (permission.getTeam() != null && permission.getTeam().getTeamUsers() != null && 
							!permission.getTeam().getTeamUsers().isEmpty()) {
					
					for (User contributor : permission.getTeam().getTeamUsers()) {
						graphElement = new HashMap<>();
						graphElement.put("@id", contributor.getSlug());
						graphElement.put("@@type", "foaf:Person");
						graphElement.put("schema:name", contributor.getName());
						graphElement.put("foaf:name", contributor.getName());
						graphElements.add(graphElement);
					}
				}
			}
		}
	}

	private Set<String> buildContributorSlugs(ComputationalModel computationalModel, Map<String, Object> graphElement) {
		Set<String> contributors = new HashSet<>();
		
		if (computationalModel.getPermissions() != null && 
				!computationalModel.getPermissions().isEmpty()) {
			
			for (Permission permission : computationalModel.getPermissions()) {
				if (permission.getUser() != null) {
					contributors.add(permission.getUser().getSlug());
				
				} else if (permission.getTeam() != null && permission.getTeam().getTeamUsers() != null && 
							!permission.getTeam().getTeamUsers().isEmpty()) {
					
					for (User user : permission.getTeam().getTeamUsers()) {
						contributors.add(user.getSlug());
					}
				}
			}
		}
		
		graphElement.put("dc:contributor", contributors);
		return contributors;
	}

	private List<Map<String, Object>> buildAggregates(ComputationalModel computationalModel,
			ModelResultMetadata modelResultMetadata) {
		List<Map<String, Object>> aggregates = new ArrayList<>();
		
		List<InstanceParam> instanceParams = instanceParamService.findAllByComputationalModel(computationalModel);
		String modelUrl = baseDomainUrl + COMPUTATIONAL_MODEL.NAME + CONTROLLER.PATH_SEPARATOR + 
				computationalModel.getSlug() + CONTROLLER.PATH_SEPARATOR;
		
		if (instanceParams != null && !instanceParams.isEmpty()) {
			
			for (InstanceParam instanceParam : instanceParams) {
				Map<String, Object> paramResource = new HashMap<>();
				String instanceParamBaselUrl = modelUrl + INSTANCE_PARAM.NAME + CONTROLLER.PATH_SEPARATOR;
				
				if (instanceParam.getValueFileId() != null && !"".equals(instanceParam.getValueFileId())) {
					instanceParamBaselUrl += instanceParam.getSlug() + CONTROLLER.PATH_SEPARATOR + INSTANCE_PARAM.VALUE_FILE_NAME;
					
				} else {
					instanceParamBaselUrl += instanceParam.getSlug();
				}
				
				paramResource.put("@id", instanceParamBaselUrl);
				paramResource.put("@type", "ro:Resource");
				aggregates.add(paramResource);
			}
		}
		
		if (modelResultMetadata.getModelExecutor() != null) {
			String executorBaselUrl = modelUrl + MODEL_EXECUTOR.NAME + CONTROLLER.PATH_SEPARATOR;
			
			if (modelResultMetadata.getModelExecutor().getExecutorFileId() != null && 
					!"".equals(modelResultMetadata.getModelExecutor().getExecutorFileId())) {
				executorBaselUrl += modelResultMetadata.getModelExecutor().getSlug() + 
						CONTROLLER.PATH_SEPARATOR + MODEL_EXECUTOR.EXECUTOR_NAME;
				
			} else {
				executorBaselUrl += modelResultMetadata.getModelExecutor().getSlug();
			}
			
			Map<String, Object> executorResource = new HashMap<>();
			executorResource.put("@id", executorBaselUrl);
			executorResource.put("@type", "ro:Resource");
			aggregates.add(executorResource);
		}
		
		String modelResultBaselUrl = modelUrl + MODEL_RESULT_METADATA.NAME + CONTROLLER.PATH_SEPARATOR;
		
		Map<String, Object> executorResource = new HashMap<>();
		executorResource.put("@id", modelResultBaselUrl + modelResultMetadata.getSlug());
		executorResource.put("@type", "ro:Resource");
		aggregates.add(executorResource);
		
		if (modelResultMetadata.getExtractorMetadatas() != null && 
				!modelResultMetadata.getExtractorMetadatas().isEmpty()) {
			
			String extractorMetadataBaselUrl = modelUrl + EXTRACTOR_METADATA.NAME + CONTROLLER.PATH_SEPARATOR;
			
			for (ExtractorMetadata extractorMetadata : modelResultMetadata.getExtractorMetadatas()) {
				
				if (extractorMetadata.getExecutionMetadataFileId() != null && 
						!"".equals(extractorMetadata.getExecutionMetadataFileId())) {
					extractorMetadataBaselUrl += extractorMetadata.getSlug() + 
							CONTROLLER.PATH_SEPARATOR + EXTRACTOR_METADATA.EXECUTION_METADATA_NAME;
					
				} else {
					extractorMetadataBaselUrl += modelResultMetadata.getModelExecutor().getSlug();
				}
				
				Map<String, Object> paramResource = new HashMap<>();
				paramResource.put("@id", extractorMetadataBaselUrl);
				paramResource.put("@type", "ro:Resource");
				aggregates.add(paramResource);
			}
		}
		
		return aggregates;
	}

	public Integer deleteByComputationalModel(ComputationalModel computationalModel) {
		List<ModelResultMetadata> modelResultMetadatas = modelResultMetadataRepository.findAllByComputationalModel(computationalModel);
		
		if (modelResultMetadatas == null || modelResultMetadatas.isEmpty()) {
			return 0;
		}
		
		Integer deletedResult = modelResultMetadataRepository.deleteByComputationalModel(computationalModel);
		
		for (ModelResultMetadata modelResultMetadata : modelResultMetadatas) {
			extractorMetadataService.deleteByModelResultMetadata(modelResultMetadata);
			
			if (modelResultMetadata.getExecutionMetadataFileId() != null && !"".equals(modelResultMetadata.getExecutionMetadataFileId())) {
				try {
					googleDriveService.deleteFileAsync(modelResultMetadata.getExecutionMetadataFileId());
					
				} catch (NotFoundApiException e) {
					log.warn(Constants.MSG_WARN.GOOGLE_FILE_NOT_FOUND, modelResultMetadata.getExecutionMetadataFileId());
					continue;
				}
			}
			
			if (modelResultMetadata.getAbortMetadataFileId() != null && !"".equals(modelResultMetadata.getAbortMetadataFileId())) {
				try {
					googleDriveService.deleteFileAsync(modelResultMetadata.getAbortMetadataFileId());
					
				} catch (NotFoundApiException e) {
					log.warn(Constants.MSG_WARN.GOOGLE_FILE_NOT_FOUND, modelResultMetadata.getAbortMetadataFileId());
					continue;
				}
			}
		}
		
		return deletedResult;
	}

	public byte[] getExecutionMetadata(String fileId) throws NotFoundApiException {
		return googleDriveService.getFileBytesContent(fileId);
	}
	
	public byte[] getAbortMetadata(String fileId) throws NotFoundApiException {
		return googleDriveService.getFileBytesContent(fileId);
	}
	
}