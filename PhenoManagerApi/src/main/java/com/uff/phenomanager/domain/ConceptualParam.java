package com.uff.phenomanager.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.uff.phenomanager.domain.core.BaseApiEntity;

@Entity
@Table(name = "conceptual_param")
public class ConceptualParam extends BaseApiEntity {
	
	@ManyToOne
	@Cascade(CascadeType.SAVE_UPDATE)
	@JoinColumn(name = "id_experiment")
	@JsonBackReference(value="experimentConceptualParams")
	private Experiment experiment;
	
	@Column(name = "key", length = 80)
	private String key;
	
	@Column(name = "description", columnDefinition = "text")
	private String description;
	
	public ConceptualParam() {}
	
	public ConceptualParam(ConceptualParamBuilder builder) {
		this.experiment = builder.experiment;
		this.key = builder.key;
		this.description = builder.description;
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

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public static ConceptualParamBuilder builder() {
		return new ConceptualParamBuilder();
	}
	
	public static class ConceptualParamBuilder extends BaseApiEntityBuilder {
		
		private Experiment experiment;
		private String key;
		private String description;
		
		public ConceptualParamBuilder experiment(Experiment experiment) {
			this.experiment = experiment;
			return this;
		}
		
		public ConceptualParamBuilder key(String key) {
			this.key = key;
			return this;
		}
		
		public ConceptualParamBuilder description(String description) {
			this.description = description;
			return this;
		}
		
		public ConceptualParam build() {
			return new ConceptualParam(this);
		}
	}

}