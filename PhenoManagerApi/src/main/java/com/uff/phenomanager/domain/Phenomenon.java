package com.uff.phenomanager.domain;

import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import com.fasterxml.jackson.annotation.JsonManagedReference;

@Entity
@Table(name = "phenomenon")
public class Phenomenon extends ResearchObject {
	
	@ManyToOne
	@Cascade(CascadeType.SAVE_UPDATE)
	@JoinColumn(name = "id_project")
	private Project project;
	
	@Column(name = "research_domain", nullable = false)
	@Enumerated(EnumType.STRING)
	private ResearchDomain researchDomain;
	
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "phenomenon")
	@Cascade(CascadeType.DELETE)
	@JsonManagedReference(value = "phenomenonPermissions")
	private Set<Permission> permissions;
	
	public Phenomenon() {
		super();
	}
	
	public Phenomenon(PhenomenonBuilder builder) {
		this.project = builder.project;
		this.researchDomain = builder.researchDomain;
		this.permissions = builder.permissions;
		this.setId(builder.getId());
		this.setSlug(builder.getSlug());
		this.setActive(builder.getActive());
		this.setInsertDate(builder.getInsertDate());
		this.setUpdateDate(builder.getUpdateDate());
		this.setDeleteDate(builder.getRemoveDate());
		this.setSum(builder.getSum());
		this.setAvg(builder.getAvg());
		this.setCount(builder.getCount());
	}

	public Project getProject() {
		return project;
	}

	public void setProject(Project project) {
		this.project = project;
	}

	public Set<Permission> getPermissions() {
		return permissions;
	}

	public void setPermissions(Set<Permission> permissions) {
		this.permissions = permissions;
	}
	
	public ResearchDomain getResearchDomain() {
		return researchDomain;
	}

	public void setResearchDomain(ResearchDomain researchDomain) {
		this.researchDomain = researchDomain;
	}

	public static PhenomenonBuilder builder() {
		return new PhenomenonBuilder();
	}
	
	public static class PhenomenonBuilder extends ResearchObjectBuilder {
		
		private Project project;
		private ResearchDomain researchDomain;
		private Set<Permission> permissions;

		public PhenomenonBuilder project(Project project) {
			this.project = project;
			return this;
		}
		
		public PhenomenonBuilder researchDomain(ResearchDomain researchDomain) {
			this.researchDomain = researchDomain;
			return this;
		}
		
		public PhenomenonBuilder permissions(Set<Permission> permissions) {
			this.permissions = permissions;
			return this;
		}
		
		public Phenomenon build() {
			return new Phenomenon(this);
		}
	}

}