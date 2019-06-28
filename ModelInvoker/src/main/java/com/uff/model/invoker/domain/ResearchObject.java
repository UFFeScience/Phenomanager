package com.uff.model.invoker.domain;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

@MappedSuperclass
public class ResearchObject extends BaseApiEntity {
	
	@Column(name = "name", length = 80)
	private String name;
	
	@Column(name = "description", columnDefinition = "text")
	private String description;
	
	@Transient
	private PermissionRole permissionRole;
	
	public ResearchObject() {}

	public ResearchObject(ResearchObjectBuilder builder) {
		this.name = builder.name;
		this.description = builder.description;
		this.permissionRole = builder.permissionRole;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	public PermissionRole getPermissionRole() {
		return permissionRole;
	}

	public void setPermissionRole(PermissionRole permissionRole) {
		this.permissionRole = permissionRole;
	}

	public static class ResearchObjectBuilder extends BaseApiEntityBuilder {
		
		protected String name;
		protected String description;
		protected PermissionRole permissionRole;
		
		public ResearchObjectBuilder name(String name) {
			this.name = name;
			return this;
		}
		
		public ResearchObjectBuilder description(String description) {
			this.description = description;
			return this;
		}
		
		public ResearchObjectBuilder permissionRole(PermissionRole permissionRole) {
			this.permissionRole = permissionRole;
			return this;
		}
		
		public ResearchObject build() {
			return new ResearchObject(this);
		}
	}

}