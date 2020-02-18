package com.uff.phenomanager.domain;

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
import com.uff.phenomanager.domain.core.BaseApiEntity;

@Entity
@Table(name = "permission")
public class Permission extends BaseApiEntity {
	
	@Column(name = "role")
	@Enumerated(EnumType.STRING)
	private PermissionRole role;
	
	@ManyToOne
	@Cascade(CascadeType.SAVE_UPDATE)
	@JoinColumn(name = "id_project")
	@JsonBackReference(value="projectPermissions")
	private Project project;
	
	@ManyToOne
	@Cascade(CascadeType.SAVE_UPDATE)
	@JoinColumn(name = "id_phenomenon")
	@JsonBackReference(value = "phenomenonPermissions")
	private Phenomenon phenomenon;
	
	@ManyToOne
	@Cascade(CascadeType.SAVE_UPDATE)
	@JoinColumn(name = "id_hypothesis")
	@JsonBackReference(value = "hypothesisPermissions")
	private Hypothesis hypothesis;
	
	@ManyToOne
	@Cascade(CascadeType.SAVE_UPDATE)
	@JoinColumn(name = "id_experiment")
	@JsonBackReference(value = "experimentPermissions")
	private Experiment experiment;
	
	@ManyToOne
	@Cascade(CascadeType.SAVE_UPDATE)
	@JoinColumn(name = "id_computational_model")
	@JsonBackReference(value = "computationalModelPermissions")
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
		this.project = builder.project;
		this.phenomenon = builder.phenomenon;
		this.hypothesis = builder.hypothesis;
		this.experiment = builder.experiment;
		this.computationalModel = builder.computationalModel;
		this.team = builder.team;
		this.user = builder.user;
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
	
	public PermissionRole getRole() {
		return role;
	}

	public void setRole(PermissionRole role) {
		this.role = role;
	}

	public Project getProject() {
		return project;
	}

	public void setProject(Project project) {
		this.project = project;
	}

	public Phenomenon getPhenomenon() {
		return phenomenon;
	}

	public void setPhenomenon(Phenomenon phenomenon) {
		this.phenomenon = phenomenon;
	}

	public Hypothesis getHypothesis() {
		return hypothesis;
	}

	public void setHypothesis(Hypothesis hypothesis) {
		this.hypothesis = hypothesis;
	}

	public Experiment getExperiment() {
		return experiment;
	}

	public void setExperiment(Experiment experiment) {
		this.experiment = experiment;
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
		private Project project;
		private Phenomenon phenomenon;
		private Hypothesis hypothesis;
		private Experiment experiment;
		private ComputationalModel computationalModel;
		private Team team;
		private User user;
		
		public PermissionBuilder role(PermissionRole role) {
			this.role = role;
			return this;
		}
		
		public PermissionBuilder project(Project project) {
			this.project = project;
			return this;
		}
		
		public PermissionBuilder phenomenon(Phenomenon phenomenon) {
			this.phenomenon = phenomenon;
			return this;
		}
		
		public PermissionBuilder hypothesis(Hypothesis hypothesis) {
			this.hypothesis = hypothesis;
			return this;
		}
		
		public PermissionBuilder experiment(Experiment experiment) {
			this.experiment = experiment;
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