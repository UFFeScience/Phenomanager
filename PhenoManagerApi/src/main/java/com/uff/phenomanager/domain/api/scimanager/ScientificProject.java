package com.uff.phenomanager.domain.api.scimanager;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ScientificProject implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private String slug;
	private Long scientificProjectId;
	private String projectName;
	private List<ScientificExperiment> scientificExperiments;
	private List<Phase> phases;
	
	public ScientificProject() {}
	
	public ScientificProject(ScientificProjectBuilder builder) {
		this.slug = builder.slug;
		this.scientificProjectId = builder.scientificProjectId;
		this.projectName = builder.projectName;
		this.scientificExperiments = builder.scientificExperiments;
		this.phases = builder.phases;
	}
	
	public String getSlug() {
		return slug;
	}

	public void setSlug(String slug) {
		this.slug = slug;
	}

	public Long getScientificProjectId() {
		return scientificProjectId;
	}

	public void setScientificProjectId(Long scientificProjectId) {
		this.scientificProjectId = scientificProjectId;
	}

	public String getProjectName() {
		return projectName;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	public List<Phase> getPhases() {
		if (phases == null) {
			phases = new ArrayList<Phase>();
		}
		
		return phases;
	}

	public void setPhases(List<Phase> phases) {
		this.phases = phases;
	}
	
	public List<ScientificExperiment> getScientificExperiments() {
		if (scientificExperiments == null) {
			scientificExperiments = new ArrayList<ScientificExperiment>();
		}
		
		return scientificExperiments;
	}

	public void setScientificExperiments(List<ScientificExperiment> scientificExperiments) {
		this.scientificExperiments = scientificExperiments;
	}

	public static ScientificProjectBuilder builder() {
		return new ScientificProjectBuilder();
	}
	
	public static class ScientificProjectBuilder {
		
		private String slug;
		private Long scientificProjectId;
		private String projectName;
		private List<ScientificExperiment> scientificExperiments;
		private List<Phase> phases;
		
		public ScientificProjectBuilder slug(String slug) {
			this.slug = slug;
			return this;
		}
		
		public ScientificProjectBuilder scientificProjectId(Long scientificProjectId) {
			this.scientificProjectId = scientificProjectId;
			return this;
		}
		
		public ScientificProjectBuilder projectName(String projectName) {
			this.projectName = projectName;
			return this;
		}
		
		public ScientificProjectBuilder scientificExperiments(List<ScientificExperiment> scientificExperiments) {
			this.scientificExperiments = scientificExperiments;
			return this;
		}
		
		public ScientificProjectBuilder phases(List<Phase> phases) {
			this.phases = phases;
			return this;
		}
		
		public ScientificProject build() {
			return new ScientificProject(this);
		}
	}

}