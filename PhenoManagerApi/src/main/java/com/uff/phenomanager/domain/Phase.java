package com.uff.phenomanager.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
@Table(name = "phase")
public class Phase extends BaseApiEntity {
	
	@Column(name = "name", length = 80)
	private String name;
	
	@ManyToOne
	@JoinColumn(name = "id_experiment", nullable = true)
	@Cascade(CascadeType.SAVE_UPDATE)
	@JsonBackReference(value = "experimentPhases")
	private Experiment experiment;
	
	public Phase() {}
	
	public Phase(PhaseBuilder builder) {
		this.name = builder.name;
		this.experiment = builder.experiment;
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
	
	public Experiment getExperiment() {
		return experiment;
	}
	
	public void setExperiment(Experiment experiment) {
		this.experiment = experiment;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public static PhaseBuilder builder() {
		return new PhaseBuilder();
	}
	
	public static class PhaseBuilder extends BaseApiEntityBuilder {
		
		private String name;
		private Experiment experiment;
		
		public PhaseBuilder name(String name) {
			this.name = name;
			return this;
		}
		
		public PhaseBuilder experiment(Experiment experiment) {
			this.experiment = experiment;
			return this;
		}
		
		public Phase build() {
			return new Phase(this);
		}
	}
	
}