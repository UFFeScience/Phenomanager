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

@Entity
@Table(name = "extractor_metadata")
public class ExtractorMetadata extends BaseApiEntity {
	
	@ManyToOne
	@Cascade(CascadeType.SAVE_UPDATE)
	@JoinColumn(name = "id_model_result_metadata")
	private ModelResultMetadata modelResultMetadata;
	
	@ManyToOne
	@Cascade(CascadeType.SAVE_UPDATE)
	@JoinColumn(name = "id_model_metadata_extractor")
	private ModelMetadataExtractor modelMetadataExtractor;
	
	@Column(name = "execution_metadata_file_id")
	private String executionMetadataFileId;
	
	@Column(name = "execution_status")
	@Enumerated(EnumType.STRING)
	private ExecutionStatus executionStatus = ExecutionStatus.SCHEDULED;
	
	public ExtractorMetadata() {}

	public ExtractorMetadata(ExtractorMetadataBuilder builder) {
		this.executionStatus = builder.executionStatus;
		this.modelResultMetadata = builder.modelResultMetadata;
		this.executionMetadataFileId = builder.executionMetadataFileId;
		this.modelMetadataExtractor = builder.modelMetadataExtractor;
		this.setId(builder.getId());
		this.setSlug(builder.getSlug());
		this.setActive(builder.getActive());
		this.setInsertDate(builder.getInsertDate());
		this.setUpdateDate(builder.getUpdateDate());
		this.setDeleteDate(builder.getRemoveDate());
	}
	
	public ModelResultMetadata getModelResultMetadata() {
		return modelResultMetadata;
	}

	public void setModelResultMetadata(ModelResultMetadata modelResultMetadata) {
		this.modelResultMetadata = modelResultMetadata;
	}
	
	public String getExecutionMetadataFileId() {
		return executionMetadataFileId;
	}

	public void setExecutionMetadataFileId(String executionMetadataFileId) {
		this.executionMetadataFileId = executionMetadataFileId;
	}

	public ModelMetadataExtractor getModelMetadataExtractor() {
		return modelMetadataExtractor;
	}

	public void setModelMetadataExtractor(ModelMetadataExtractor modelMetadataExtractor) {
		this.modelMetadataExtractor = modelMetadataExtractor;
	}
	
	public ExecutionStatus getExecutionStatus() {
		return executionStatus;
	}

	public void setExecutionStatus(ExecutionStatus executionStatus) {
		this.executionStatus = executionStatus;
	}
	
	public static ExtractorMetadataBuilder builder() {
		return new ExtractorMetadataBuilder();
	}
	
	public static class ExtractorMetadataBuilder extends BaseApiEntityBuilder {
		
		private ModelResultMetadata modelResultMetadata;
		private String executionMetadataFileId;
		private ModelMetadataExtractor modelMetadataExtractor;
		private ExecutionStatus executionStatus = ExecutionStatus.SCHEDULED;
		
		public ExtractorMetadataBuilder executionStatus(ExecutionStatus executionStatus) {
			this.executionStatus = executionStatus;
			return this;
		}
		
		public ExtractorMetadataBuilder modelResultMetadata(ModelResultMetadata modelResultMetadata) {
			this.modelResultMetadata = modelResultMetadata;
			return this;
		}
		
		public ExtractorMetadataBuilder executionMetadataFileId(String executionMetadataFileId) {
			this.executionMetadataFileId = executionMetadataFileId;
			return this;
		}
		
		public ExtractorMetadataBuilder modelMetadataExtractor(ModelMetadataExtractor modelMetadataExtractor) {
			this.modelMetadataExtractor = modelMetadataExtractor;
			return this;
		}
		
		public ExtractorMetadata build() {
			return new ExtractorMetadata(this);
		}
	}
	
}