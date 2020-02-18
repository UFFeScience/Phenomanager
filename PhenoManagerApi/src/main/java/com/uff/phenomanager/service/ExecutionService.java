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
import com.uff.phenomanager.Constants.CONTROLLER.EXECUTION;
import com.uff.phenomanager.Constants.CONTROLLER.EXECUTOR;
import com.uff.phenomanager.Constants.CONTROLLER.EXTRACTOR_EXECUTION;
import com.uff.phenomanager.Constants.CONTROLLER.INSTANCE_PARAM;
import com.uff.phenomanager.Constants.MSG_ERROR;
import com.uff.phenomanager.domain.ComputationalModel;
import com.uff.phenomanager.domain.Environment;
import com.uff.phenomanager.domain.Execution;
import com.uff.phenomanager.domain.ExecutionStatus;
import com.uff.phenomanager.domain.Executor;
import com.uff.phenomanager.domain.ExtractorExecution;
import com.uff.phenomanager.domain.InstanceParam;
import com.uff.phenomanager.domain.Permission;
import com.uff.phenomanager.domain.User;
import com.uff.phenomanager.domain.core.filter.FilterOperator;
import com.uff.phenomanager.domain.core.filter.RequestFilter;
import com.uff.phenomanager.exception.ApiException;
import com.uff.phenomanager.exception.NotFoundApiException;
import com.uff.phenomanager.exception.UnauthorizedApiException;
import com.uff.phenomanager.repository.ExecutionRepository;
import com.uff.phenomanager.service.api.google.GoogleDriveService;
import com.uff.phenomanager.service.core.ApiPermissionRestService;
import com.uff.phenomanager.service.core.TokenAuthenticationService;
import com.uff.phenomanager.util.TokenUtils;

@Service
public class ExecutionService extends ApiPermissionRestService<Execution, ExecutionRepository> {

	private static final Logger log = LoggerFactory.getLogger(ExecutionService.class);
	
	@Autowired
	private ExecutionRepository executionRepository;
	
	@Autowired
	private ComputationalModelService computationalModelService;
	
	@Autowired
	private InstanceParamService instanceParamService;
	
	@Autowired
	private ExtractorExecutionService extractorExecutionService;
	
	@Autowired
	private TokenAuthenticationService tokenAuthenticationService;
	
	@Lazy
	@Autowired
	private GoogleDriveService googleDriveService;
	
	@Value(Constants.BASE_DOMAIN_URL)
	private String baseDomainUrl;
	
	@Override
	protected ExecutionRepository getRepository() {
		return executionRepository;
	}
	
	@Override
	protected Class<Execution> getEntityClass() {
		return Execution.class;
	}
	
	@Override
	protected String getPermissionEntityName() {
		return StringUtils.uncapitalize(ComputationalModel.class.getSimpleName());
	}
	
	public Execution findBySlug(String slug, String authorization, String computationalModelSlug) throws ApiException {
		Execution entity = findBySlug(slug);
		ComputationalModel computationalModel = computationalModelService.findBySlug(computationalModelSlug);
		
		if (computationalModel.getIsPublicData()) {
			return entity;
		}
		Boolean hasAuthorization = Boolean.TRUE;
		
        if (authorization == null || "".equals(authorization)) {
        	hasAuthorization = Boolean.FALSE;
		
        } else if (!tokenAuthenticationService.validateToken(authorization)) {
			hasAuthorization = Boolean.FALSE;
		
        } else if (!allowPermissionReadAccess(authorization, computationalModelSlug)) {
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
		Execution execution = findBySlug(slug, authorization, computationalModel.getSlug());
		
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
		graphElement.put("dc:creator", execution.getUserAgent().getSlug());
		graphElements.add(graphElement);
		
		graphElement = new HashMap<>();
		graphElement.put("dc:abstract", computationalModel.getDescription() != null ? computationalModel.getDescription() : "");
		graphElements.add(graphElement);
		
		graphElement = new HashMap<>();
		
		Set<String> contributorSlugs = buildContributorSlugs(computationalModel, graphElement);
		
		graphElements.add(graphElement);
		
		graphElement = new HashMap<>();
		graphElement.put("dc:title", computationalModel.getName());
		graphElements.add(graphElement);
		
		graphElement = new HashMap<>();
		graphElement.put("ore:aggregates", buildAggregates(computationalModel, execution));
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
			Execution execution) {
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
		
		if (execution.getExecutor() != null) {
			String executorBaselUrl = modelUrl + EXECUTOR.NAME + CONTROLLER.PATH_SEPARATOR;
			
			if (execution.getExecutor().getFileId() != null && 
					!"".equals(execution.getExecutor().getFileId())) {
				executorBaselUrl += execution.getExecutor().getSlug() + 
						CONTROLLER.PATH_SEPARATOR + EXECUTOR.EXECUTOR_FILE_NAME;
				
			} else {
				executorBaselUrl += execution.getExecutor().getSlug();
			}
			
			Map<String, Object> executorResource = new HashMap<>();
			executorResource.put("@id", executorBaselUrl);
			executorResource.put("@type", "ro:Resource");
			aggregates.add(executorResource);
		}
		
		String executionBaselUrl = modelUrl + EXECUTION.NAME + CONTROLLER.PATH_SEPARATOR;
		
		Map<String, Object> executorResource = new HashMap<>();
		executorResource.put("@id", executionBaselUrl + execution.getSlug());
		executorResource.put("@type", "ro:Resource");
		aggregates.add(executorResource);
		
		if (execution.getExtractorExecutions() != null && 
				!execution.getExtractorExecutions().isEmpty()) {
			
			String extractorExecutionBaselUrl = modelUrl + EXTRACTOR_EXECUTION.NAME + CONTROLLER.PATH_SEPARATOR;
			
			for (ExtractorExecution extractorExecution : execution.getExtractorExecutions()) {
				
				if (extractorExecution.getExecutionMetadataFileId() != null && 
						!"".equals(extractorExecution.getExecutionMetadataFileId())) {
					extractorExecutionBaselUrl += extractorExecution.getSlug() + 
							CONTROLLER.PATH_SEPARATOR + EXTRACTOR_EXECUTION.EXECUTION_METADATA_NAME;
					
				} else {
					extractorExecutionBaselUrl += execution.getExecutor().getSlug();
				}
				
				Map<String, Object> paramResource = new HashMap<>();
				paramResource.put("@id", extractorExecutionBaselUrl);
				paramResource.put("@type", "ro:Resource");
				aggregates.add(paramResource);
			}
		}
		
		return aggregates;
	}

	public Integer deleteByComputationalModel(ComputationalModel computationalModel) {
		List<Execution> executions = executionRepository.findAllByComputationalModel(computationalModel);
		
		if (executions == null || executions.isEmpty()) {
			return 0;
		}
		
		Integer deletedResult = executionRepository.deleteByComputationalModel(computationalModel);
		
		for (Execution execution : executions) {
			extractorExecutionService.deleteByExecution(execution);
			
			if (execution.getExecutionMetadataFileId() != null && !"".equals(execution.getExecutionMetadataFileId())) {
				try {
					googleDriveService.deleteFileAsync(execution.getExecutionMetadataFileId());
					
				} catch (NotFoundApiException e) {
					log.warn(Constants.MSG_WARN.GOOGLE_FILE_NOT_FOUND, execution.getExecutionMetadataFileId());
					continue;
				}
			}
			
			if (execution.getAbortionMetadataFileId() != null && !"".equals(execution.getAbortionMetadataFileId())) {
				try {
					googleDriveService.deleteFileAsync(execution.getAbortionMetadataFileId());
					
				} catch (NotFoundApiException e) {
					log.warn(Constants.MSG_WARN.GOOGLE_FILE_NOT_FOUND, execution.getAbortionMetadataFileId());
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
	
	public Long countByEnvironmentAndStatus(Environment environment, ExecutionStatus status) {
		return executionRepository.countByEnvironmentAndStatus(environment, status);
	}

	public Long countByExecutorAndStatus(Executor executor, ExecutionStatus status) {
		return executionRepository.countByExecutorAndStatus(executor, status);
	}

	public Long countByExecutorAndEnvironmentAndStatus(Executor executor, Environment environment, ExecutionStatus status) {
		return executionRepository.countByExecutorAndEnvironmentAndStatus(executor, environment, status);
	}
	
	public Map<String, Long> countAllRunningModels(String authorization) throws ApiException {
		RequestFilter runningFilter = new RequestFilter();
		runningFilter.addAndFilter("status", ExecutionStatus.RUNNING, FilterOperator.EQ);
		
		Map<String, Long> totalRunningModels = new HashMap<>();
		totalRunningModels.put("totalRunningModels", countAll(runningFilter, authorization));
		
		return totalRunningModels;
	}

	public Map<String, Long> countAllErrorModels(String authorization) throws ApiException {
		RequestFilter errorFilter = new RequestFilter();
		errorFilter.addAndFilter("status", ExecutionStatus.FAILURE, FilterOperator.EQ);
		
		Map<String, Long> totalErrorModels = new HashMap<>();
		totalErrorModels.put("totalErrorModels", countAll(errorFilter, authorization));
		
		return totalErrorModels;
	}
	
}