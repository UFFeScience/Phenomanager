package com.uff.phenomanager.domain;

import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import com.fasterxml.jackson.annotation.JsonManagedReference;

@Entity
@Table(name = "experiment")
public class Experiment extends ResearchObject {
	
	@ManyToOne
	@Cascade(CascadeType.SAVE_UPDATE)
	@JoinColumn(name = "id_hypothesis")
	private Hypothesis hypothesis;
	
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "experiment")
	@Cascade(CascadeType.DELETE)
	@JsonManagedReference(value = "experimentPermissions")
	private Set<Permission> permissions;
	
	public Experiment() {
		super();
	}
	
	public Experiment(ExperimentBuilder builder) {
		this.hypothesis = builder.hypothesis;
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
		this.setCountDistinct(builder.getCountDistinct());
	}
	

	public Hypothesis getHypothesis() {
		return hypothesis;
	}

	public void setHypothesis(Hypothesis hypothesis) {
		this.hypothesis = hypothesis;
	}

	public Set<Permission> getPermissions() {
		return permissions;
	}

	public void setPermissions(Set<Permission> permissions) {
		this.permissions = permissions;
	}

	public static ExperimentBuilder builder() {
		return new ExperimentBuilder();
	}
	
	public static class ExperimentBuilder extends ResearchObjectBuilder {
		
		private Hypothesis hypothesis;
		private Set<Permission> permissions;

		public ExperimentBuilder hypothesis(Hypothesis hypothesis) {
			this.hypothesis = hypothesis;
			return this;
		}
		
		public ExperimentBuilder permissions(Set<Permission> permissions) {
			this.permissions = permissions;
			return this;
		}
		
		public Experiment build() {
			return new Experiment(this);
		}
	}

}