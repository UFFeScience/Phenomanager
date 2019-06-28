package com.uff.model.invoker.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
@Table(name = "permission")
public class Permission extends BaseApiEntity {
	
	@Column(name = "role")
	@Enumerated(EnumType.STRING)
	private PermissionRole role;
	
	@ManyToOne
	@Cascade(CascadeType.SAVE_UPDATE)
	@JoinColumn(name = "id_computational_model")
	@JsonBackReference
	private ComputationalModel computationalModel;
	
	@ManyToOne
	@Cascade(CascadeType.SAVE_UPDATE)
	@JoinColumn(name = "id_team")
	private Team team;
	
	@ManyToOne
	@Cascade(CascadeType.SAVE_UPDATE)
	@JoinColumn(name = "id_user", referencedColumnName = "id")
	private User user;
	
	public Permission() {}
	
	public Permission(PermissionBuilder builder) {
		this.role = builder.role;
		this.computationalModel = builder.computationalModel;
		this.team = builder.team;
		this.user = builder.user;
		this.setId(builder.getId());
		this.setSlug(builder.getSlug());
		this.setActive(builder.getActive());
		this.setInsertDate(builder.getInsertDate());
		this.setUpdateDate(builder.getUpdateDate());
		this.setDeleteDate(builder.getRemoveDate());
	}
	
	public PermissionRole getRole() {
		return role;
	}

	public void setRole(PermissionRole role) {
		this.role = role;
	}

	public ComputationalModel getComputationalModel() {
		return computationalModel;
	}

	public void setComputationalModel(ComputationalModel computationalModel) {
		this.computationalModel = computationalModel;
	}
	
	public Team getTeam() {
		return team;
	}

	public void setTeam(Team team) {
		this.team = team;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public static PermissionBuilder builder() {
		return new PermissionBuilder();
	}
	
	public static class PermissionBuilder extends BaseApiEntityBuilder {
		
		private PermissionRole role;
		private ComputationalModel computationalModel;
		private Team team;
		private User user;
		
		public PermissionBuilder role(PermissionRole role) {
			this.role = role;
			return this;
		}
		
		public PermissionBuilder computationalModel(ComputationalModel computationalModel) {
			this.computationalModel = computationalModel;
			return this;
		}
		
		public PermissionBuilder team(Team team) {
			this.team = team;
			return this;
		}
		
		public PermissionBuilder user(User user) {
			this.user = user;
			return this;
		}
		
		public Permission build() {
			return new Permission(this);
		}
	}

}