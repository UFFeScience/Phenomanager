package com.uff.phenomanager.domain.api.scimanager;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ScientificExperiment implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private String slug;
	private Long scientificExperimentId;
	private String experimentName;
	private ScientificProject scientificProject;
	private List<Workflow> workflows;
	
	public ScientificExperiment() {}
	
	public ScientificExperiment(ScientificExperimentBuilder builder) {
		this.slug = builder.slug;
		this.scientificExperimentId = builder.scientificExperimentId;
		this.experimentName = builder.experimentName;
		this.scientificProject = builder.scientificProject;
		this.workflows = builder.workflows;
	}
	
	public String getSlug() {
		return slug;
	}

	public void setSlug(String slug) {
		this.slug = slug;
	}

	public List<Workflow> getWorkflows() {
		if (workflows == null) {
			workflows = new ArrayList<Workflow>();
		}
		
		return workflows;
	}

	public Long getScientificExperimentId() {
		return scientificExperimentId;
	}

	public void setScientificExperimentId(Long scientificExperimentId) {
		this.scientificExperimentId = scientificExperimentId;
	}

	public String getExperimentName() {
		return experimentName;
	}

	public void setExperimentName(String experimentName) {
		this.experimentName = experimentName;
	}

	public ScientificProject getScientificProject() {
		return scientificProject;
	}

	public void setScientificProject(ScientificProject scientificProject) {
		this.scientificProject = scientificProject;
	}

	public void setWorkflows(List<Workflow> workflows) {
		this.workflows = workflows;
	}
	
	public static ScientificExperimentBuilder builder() {
		return new ScientificExperimentBuilder();
	}
	
	public static class ScientificExperimentBuilder {
		
		private String slug;
		private Long scientificExperimentId;
		private String experimentName;
		private ScientificProject scientificProject;
		private List<Workflow> workflows;
		
		public ScientificExperimentBuilder slug(String slug) {
			this.slug = slug;
			return this;
		}
		
		public ScientificExperimentBuilder scientificExperimentId(Long scientificExperimentId) {
			this.scientificExperimentId = scientificExperimentId;
			return this;
		}
		
		public ScientificExperimentBuilder scientificProject(ScientificProject scientificProject) {
			this.scientificProject = scientificProject;
			return this;
		}
		
		public ScientificExperimentBuilder experimentName(String experimentName) {
			this.experimentName = experimentName;
			return this;
		}
		
		public ScientificExperimentBuilder workflows(List<Workflow> workflows) {
			this.workflows = workflows;
			return this;
		}
		
		public ScientificExperiment build() {
			return new ScientificExperiment(this);
		}
	}

}