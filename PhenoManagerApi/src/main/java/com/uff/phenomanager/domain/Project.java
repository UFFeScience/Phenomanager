package com.uff.phenomanager.domain;

import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import com.fasterxml.jackson.annotation.JsonManagedReference;

@Entity
@Table(name = "project")
public class Project extends ResearchObject {
	
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "project")
	@Cascade(CascadeType.DELETE)
	@JsonManagedReference(value = "projectPermissions")
	private Set<Permission> permissions;
	
	public Project() {}
	
	public Project(ProjectBuilder builder) {
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
	
	public Set<Permission> getPermissions() {
		return permissions;
	}

	public void setPermissions(Set<Permission> permissions) {
		this.permissions = permissions;
	}

	public static ProjectBuilder builder() {
		return new ProjectBuilder();
	}
	
	public static class ProjectBuilder extends ResearchObjectBuilder {
		
		private Set<Permission> permissions;

		public ProjectBuilder permissions(Set<Permission> permissions) {
			this.permissions = permissions;
			return this;
		}
		
		public Project build() {
			return new Project(this);
		}
	}

}