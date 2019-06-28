package com.uff.phenomanager.domain.api.scimanager;

import java.io.Serializable;

public class Phase implements Serializable{

	private static final long serialVersionUID = 1L;
	
	private String slug;
	private Long phaseId;
	private String phaseName;
	private Boolean allowExecution = Boolean.FALSE;
	private ScientificProject scientificProject;
	
	public Phase() {}
	
	public Phase(PhaseBuilder builder) {
		this.slug = builder.slug;
		this.phaseId = builder.phaseId;
		this.phaseName = builder.phaseName;
		this.allowExecution = builder.allowExecution;
		this.scientificProject = builder.scientificProject;
	}
	
	public String getSlug() {
		return slug;
	}

	public void setSlug(String slug) {
		this.slug = slug;
	}

	public Long getPhaseId() {
		return phaseId;
	}

	public void setPhaseId(Long phaseId) {
		this.phaseId = phaseId;
	}

	public String getPhaseName() {
		return phaseName;
	}

	public void setPhaseName(String phaseName) {
		this.phaseName = phaseName;
	}
	
	public Boolean getAllowExecution() {
		return allowExecution;
	}

	public void setAllowExecution(Boolean allowExecution) {
		this.allowExecution = allowExecution;
	}

	public ScientificProject getScientificProject() {
		return scientificProject;
	}

	public void setScientificProject(ScientificProject scientificProject) {
		this.scientificProject = scientificProject;
	}
	
	public static PhaseBuilder builder() {
		return new PhaseBuilder();
	}
	
	public static class PhaseBuilder {
		
		private String slug;
		private Long phaseId;
		private String phaseName;
		private Boolean allowExecution = Boolean.FALSE;
		private ScientificProject scientificProject;
		
		public PhaseBuilder slug(String slug) {
			this.slug = slug;
			return this;
		}
		
		public PhaseBuilder phaseId(Long phaseId) {
			this.phaseId = phaseId;
			return this;
		}
		
		public PhaseBuilder phaseName(String phaseName) {
			this.phaseName = phaseName;
			return this;
		}
		
		public PhaseBuilder allowExecution(Boolean allowExecution) {
			this.allowExecution = allowExecution;
			return this;
		}
		
		public PhaseBuilder scientificProject(ScientificProject scientificProject) {
			this.scientificProject = scientificProject;
			return this;
		}
		
		public Phase build() {
			return new Phase(this);
		}
	}
	
}