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
@Table(name = "extractor_execution")
public class ExtractorExecution extends BaseApiEntity {
	
	@ManyToOne
	@Cascade(CascadeType.SAVE_UPDATE)
	@JoinColumn(name = "id_execution")
	@JsonBackReference(value = "extractorExecutions")
	private Execution execution;
	
	@ManyToOne
	@Cascade(CascadeType.SAVE_UPDATE)
	@JoinColumn(name = "id_extractor")
	private Extractor extractor;
	
	@Column(name = "execution_metadata_file_id")
	private String executionMetadataFileId;
	
	@Column(name = "status")
	@Enumerated(EnumType.STRING)
	private ExecutionStatus status = ExecutionStatus.SCHEDULED;
	
	public ExtractorExecution() {}

	public ExtractorExecution(ExtractorExecutionBuilder builder) {
		this.status = builder.status;
		this.execution = builder.execution;
		this.executionMetadataFileId = builder.executionMetadataFileId;
		this.extractor = builder.extractor;
		this.setId(builder.getId());
		this.setSlug(builder.getSlug());
		this.setActive(builder.getActive());
		this.setInsertDate(builder.getInsertDate());
		this.setUpdateDate(builder.getUpdateDate());
		this.setDeleteDate(builder.getRemoveDate());
	}
	
	public Execution getExecution() {
		return execution;
	}

	public void setExecution(Execution execution) {
		this.execution = execution;
	}
	
	public String getExecutionMetadataFileId() {
		return executionMetadataFileId;
	}

	public void setExecutionMetadataFileId(String executionMetadataFileId) {
		this.executionMetadataFileId = executionMetadataFileId;
	}

	public Extractor getExtractor() {
		return extractor;
	}

	public void setExtractor(Extractor extractor) {
		this.extractor = extractor;
	}
	
	public ExecutionStatus getStatus() {
		return status;
	}

	public void setStatus(ExecutionStatus status) {
		this.status = status;
	}

	public static ExtractorExecutionBuilder builder() {
		return new ExtractorExecutionBuilder();
	}
	
	public static class ExtractorExecutionBuilder extends BaseApiEntityBuilder {
		
		private Execution execution;
		private String executionMetadataFileId;
		private Extractor extractor;
		private ExecutionStatus status = ExecutionStatus.SCHEDULED;
		
		public ExtractorExecutionBuilder status(ExecutionStatus status) {
			this.status = status;
			return this;
		}
		
		public ExtractorExecutionBuilder execution(Execution execution) {
			this.execution = execution;
			return this;
		}
		
		public ExtractorExecutionBuilder executionMetadataFileId(String executionMetadataFileId) {
			this.executionMetadataFileId = executionMetadataFileId;
			return this;
		}
		
		public ExtractorExecutionBuilder extractor(Extractor extractor) {
			this.extractor = extractor;
			return this;
		}
		
		public ExtractorExecution build() {
			return new ExtractorExecution(this);
		}
	}
	
}