package com.uff.model.invoker.domain;

import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

@Entity
@Table(name = "computational_model")
public class ComputationalModel extends ResearchObject {
	
	@Column(name = "type", nullable = false)
	@Enumerated(EnumType.STRING)
	private ModelType type;
	
	@Column(name = "current_version", length = 32)
	private String currentVersion = "1.0";

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "computationalModel")
	@Cascade(CascadeType.DELETE)
	private Set<Permission> permissions;
	
	@Column(name = "is_public_data")
	private Boolean isPublicData = Boolean.FALSE;
	
	public ComputationalModel() {
		super();
	}
	
	public ComputationalModel(ComputationalModelBuilder builder) {
		this.type = builder.type;
		this.currentVersion = builder.currentVersion;
		this.permissions = builder.permissions;
		this.isPublicData = builder.isPublicData;
		this.setName(builder.name);
		this.setDescription(builder.description);
		this.setPermissionRole(builder.permissionRole);
		this.setId(builder.getId());
		this.setSlug(builder.getSlug());
		this.setActive(builder.getActive());
		this.setInsertDate(builder.getInsertDate());
		this.setUpdateDate(builder.getUpdateDate());
		this.setDeleteDate(builder.getRemoveDate());
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
		
		public ComputationalModelBuilder permissions(Set<Permission> permissions) {
			this.permissions = permissions;
			return this;
		}
		
		public ComputationalModel build() {
			return new ComputationalModel(this);
		}
	}

}