package com.uff.phenomanager.domain.api.scimanager;

import java.io.Serializable;

public class Workflow implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private String slug;
	private Long workflowId;
	private String workflowName;
	private String swfms;
	private String currentVersion = "1.0";
	private ScientificExperiment scientificExperiment;
	private ScientificProject scientificProject;
	private UserGroup responsibleGroup;
	
	public Workflow() {}
	
	public Workflow(WorkflowBuilder builder) {
		this.slug = builder.slug;
		this.workflowId = builder.workflowId;
		this.workflowName = builder.workflowName;
		this.swfms = builder.swfms;
		this.currentVersion = builder.currentVersion;
		this.scientificExperiment = builder.scientificExperiment;
		this.scientificProject = builder.scientificProject;
		this.responsibleGroup = builder.responsibleGroup;
	}
	
	public String getSlug() {
		return slug;
	}

	public void setSlug(String slug) {
		this.slug = slug;
	}

	public Long getWorkflowId() {
		return workflowId;
	}

	public void setWorkflowId(Long workflowId) {
		this.workflowId = workflowId;
	}

	public String getWorkflowName() {
		return workflowName;
	}

	public void setWorkflowName(String workflowName) {
		this.workflowName = workflowName;
	}

	public String getSwfms() {
		return swfms;
	}

	public void setSwfms(String swfms) {
		this.swfms = swfms;
	}
	
	public String getCurrentVersion() {
		return currentVersion;
	}

	public void setCurrentVersion(String currentVersion) {
		this.currentVersion = currentVersion;
	}
	
	public ScientificExperiment getScientificExperiment() {
		return scientificExperiment;
	}

	public void setScientificExperiment(ScientificExperiment scientificExperiment) {
		this.scientificExperiment = scientificExperiment;
	}

	public ScientificProject getScientificProject() {
		return scientificProject;
	}

	public void setScientificProject(ScientificProject scientificProject) {
		this.scientificProject = scientificProject;
	}

	public UserGroup getResponsibleGroup() {
		return responsibleGroup;
	}

	public void setResponsibleGroup(UserGroup responsibleGroup) {
		this.responsibleGroup = responsibleGroup;
	}
	
	public static WorkflowBuilder builder() {
		return new WorkflowBuilder();
	}
	
	public static class WorkflowBuilder {
		
		private String slug;
		private Long workflowId;
		private String workflowName;
		private String swfms;
		private String currentVersion = "1.0";
		private ScientificExperiment scientificExperiment;
		private ScientificProject scientificProject;
		private UserGroup responsibleGroup;
		
		public WorkflowBuilder slug(String slug) {
			this.slug = slug;
			return this;
		}
		
		public WorkflowBuilder workflowId(Long workflowId) {
			this.workflowId = workflowId;
			return this;
		}
		
		public WorkflowBuilder workflowName(String workflowName) {
			this.workflowName = workflowName;
			return this;
		}
		
		public WorkflowBuilder swfms(String swfms) {
			this.swfms = swfms;
			return this;
		}
		
		public WorkflowBuilder currentVersion(String currentVersion) {
			this.currentVersion = currentVersion;
			return this;
		}
		
		public WorkflowBuilder scientificExperiment(ScientificExperiment scientificExperiment) {
			this.scientificExperiment = scientificExperiment;
			return this;
		}
		
		public WorkflowBuilder scientificProject(ScientificProject scientificProject) {
			this.scientificProject = scientificProject;
			return this;
		}
		
		public WorkflowBuilder responsibleGroup(UserGroup responsibleGroup) {
			this.responsibleGroup = responsibleGroup;
			return this;
		}
		
		public Workflow build() {
			return new Workflow(this);
		}
	}

}