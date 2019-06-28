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
@Table(name = "validation_item")
public class ValidationItem extends BaseApiEntity {
	
	@ManyToOne
	@Cascade(CascadeType.SAVE_UPDATE)
	@JoinColumn(name = "id_experiment")
	@JsonBackReference(value = "experimentValidationItem")
	private Experiment experiment;
	
	@Column(name = "validated")
	private Boolean validated = Boolean.FALSE;
	
	@Column(name = "expected_value_description", columnDefinition = "text")
	private String expectedValueDescription;
	
	@Column(name = "validation_evidence_file_id")
	private String validationEvidenceFileId;
	
	@Column(name = "validation_evidence_file_content_type")
	private String validationEvidenceFileContentType;
	
	@Column(name = "validation_evidence_file_name")
	private String validationEvidenceFileName;
	
	public ValidationItem() {}
	
	public ValidationItem(ValidationItemBuilder builder) {
		this.experiment = builder.experiment;
		this.validated = builder.validated;
		this.expectedValueDescription = builder.expectedValueDescription;
		this.validationEvidenceFileId = builder.validationEvidenceFileId;
		this.validationEvidenceFileContentType = builder.validationEvidenceFileContentType;
		this.validationEvidenceFileName = builder.validationEvidenceFileName;
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

	public Boolean getValidated() {
		return validated;
	}

	public void setValidated(Boolean validated) {
		this.validated = validated;
	}

	public String getExpectedValueDescription() {
		return expectedValueDescription;
	}

	public void setExpectedValueDescription(String expectedValueDescription) {
		this.expectedValueDescription = expectedValueDescription;
	}
	
	public String getValidationEvidenceFileName() {
		return validationEvidenceFileName;
	}

	public void setValidationEvidenceFileName(String validationEvidenceFileName) {
		this.validationEvidenceFileName = validationEvidenceFileName;
	}
	
	public String getValidationEvidenceFileId() {
		return validationEvidenceFileId;
	}

	public void setValidationEvidenceFileId(String validationEvidenceFileId) {
		this.validationEvidenceFileId = validationEvidenceFileId;
	}

	public String getValidationEvidenceFileContentType() {
		return validationEvidenceFileContentType;
	}

	public void setValidationEvidenceFileContentType(String validationEvidenceFileContentType) {
		this.validationEvidenceFileContentType = validationEvidenceFileContentType;
	}

	public static ValidationItemBuilder builder() {
		return new ValidationItemBuilder();
	}
	
	public static class ValidationItemBuilder extends BaseApiEntityBuilder {
		
		private Experiment experiment;
		private Boolean validated = Boolean.FALSE;
		private String expectedValueDescription;
		private String validationEvidenceFileId;
		private String validationEvidenceFileContentType;
		private String validationEvidenceFileName;
		
		public ValidationItemBuilder experiment(Experiment experiment) {
			this.experiment = experiment;
			return this;
		}
		
		public ValidationItemBuilder validated(Boolean validated) {
			this.validated = validated;
			return this;
		}
		
		public ValidationItemBuilder expectedValueDescription(String expectedValueDescription) {
			this.expectedValueDescription = expectedValueDescription;
			return this;
		}
		
		public ValidationItemBuilder validationEvidenceFileContentType(String validationEvidenceFileContentType) {
			this.validationEvidenceFileContentType = validationEvidenceFileContentType;
			return this;
		}
		
		public ValidationItemBuilder validationEvidenceFileId(String validationEvidenceFileId) {
			this.validationEvidenceFileId = validationEvidenceFileId;
			return this;
		}
		
		public ValidationItemBuilder validationEvidenceFileName(String validationEvidenceFileName) {
			this.validationEvidenceFileName = validationEvidenceFileName;
			return this;
		}
		
		public ValidationItem build() {
			return new ValidationItem(this);
		}
	}

}