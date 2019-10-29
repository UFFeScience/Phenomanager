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
@Table(name = "computational_model")
public class ComputationalModel extends ResearchObject {
	
	@Column(name = "type")
	@Enumerated(EnumType.STRING)
	private ModelType type;
	
	@Column(name = "current_version", length = 32)
	private String currentVersion = "1.0";
	
	@ManyToOne
	@Cascade(CascadeType.SAVE_UPDATE)
	@JoinColumn(name = "id_experiment")
	private Experiment experiment;
	
	@Column(name = "is_public_data")
	private Boolean isPublicData = Boolean.FALSE;
	
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "computationalModel")
	@Cascade(CascadeType.DELETE)
	@JsonManagedReference(value = "computationalModelPermissions")
	private Set<Permission> permissions;
	
	public ComputationalModel() {
		super();
	}
	
	public ComputationalModel(ComputationalModelBuilder builder) {
		this.type = builder.type;
		this.currentVersion = builder.currentVersion;
		this.experiment = builder.experiment;
		this.isPublicData = builder.isPublicData;
		this.permissions = builder.permissions;
		this.setName(builder.name);
		this.setDescription(builder.description);
		this.setId(builder.getId());
		this.setSlug(builder.getSlug());
		this.setActive(builder.getActive());
		this.setInsertDate(builder.getInsertDate());
		this.setUpdateDate(builder.getUpdateDate());
		this.setDeleteDate(builder.getRemoveDate());
		this.setSum(builder.getSum());
		this.setAvg(builder.getAvg());
		this.setCount(builder.getCount());
		this.setCountDistinct(builder.getCountDistinct());
	}
	
	public ModelType getType() {
		return type;
	}

	public void setType(ModelType type) {
		this.type = type;
	}

	public String getCurrentVersion() {
		return currentVersion;
	}

	public void setCurrentVersion(String currentVersion) {
		this.currentVersion = currentVersion;
	}
	
	public Experiment getExperiment() {
		return experiment;
	}

	public void setExperiment(Experiment experiment) {
		this.experiment = experiment;
	}

	public Set<Permission> getPermissions() {
		return permissions;
	}

	public void setPermissions(Set<Permission> permissions) {
		this.permissions = permissions;
	}
	
	public Boolean getIsPublicData() {
		return isPublicData;
	}

	public void setIsPublicData(Boolean isPublicData) {
		this.isPublicData = isPublicData;
	}

	public static ComputationalModelBuilder builder() {
		return new ComputationalModelBuilder();
	}
	
	public static class ComputationalModelBuilder extends ResearchObjectBuilder {
		
		private ModelType type;
		private String currentVersion = "1.0";
		private Experiment experiment;
		private Set<Permission> permissions;
		private Boolean isPublicData = Boolean.FALSE;

		public ComputationalModelBuilder type(ModelType type) {
			this.type = type;
			return this;
		}
		
		public ComputationalModelBuilder isPublicData(Boolean isPublicData) {
			this.isPublicData = isPublicData;
			return this;
		}
		
		public ComputationalModelBuilder currentVersion(String currentVersion) {
			this.currentVersion = currentVersion;
			return this;
		}
		
		public ComputationalModelBuilder experiment(Experiment experiment) {
			this.experiment = experiment;
			return this;
		}
		
		public ComputationalModelBuilder permissions(Set<Permission> permissions) {
			this.permissions = permissions;
			return this;
		}
		
		public ComputationalModel build() {
			return new ComputationalModel(this);
		}
	}

}