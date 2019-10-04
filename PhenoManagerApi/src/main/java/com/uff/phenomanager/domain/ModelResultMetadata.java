package com.uff.phenomanager.domain;

import java.util.Calendar;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import com.fasterxml.jackson.annotation.JsonManagedReference;

@Entity
@Table(name = "model_result_metadata")
public class ModelResultMetadata extends BaseApiEntity {
	
	@ManyToOne
	@Cascade(CascadeType.SAVE_UPDATE)
	@JoinColumn(name = "id_model_executor")
	private ModelExecutor modelExecutor;
	
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "modelResultMetadata")
	@Cascade(CascadeType.DELETE)
	@JsonManagedReference(value = "modelResultMetadataExtractorMetadatas")
	private Set<ExtractorMetadata> extractorMetadatas;
	
	@Column(name = "execution_metadata_file_id")
	private String executionMetadataFileId;
	
	@Column(name = "abort_metadata_file_id")
	private String abortMetadataFileId;
	
	@ManyToOne
	@Cascade(CascadeType.SAVE_UPDATE)
	@JoinColumn(name = "id_computational_model")
	private ComputationalModel computationalModel;
	
	@ManyToOne
	@Cascade(CascadeType.SAVE_UPDATE)
	@JoinColumn(name = "id_execution_environment")
	private ExecutionEnvironment executionEnvironment;
	
	@ManyToOne
	@Cascade(CascadeType.SAVE_UPDATE)
	@JoinColumn(name = "id_user_account_agent", referencedColumnName = "id")
	private User userAgent;
	
	@Column(name = "execution_start_date")
	private Calendar executionStartDate;
	
	@Column(name = "execution_finish_date")
	private Calendar executionFinishDate;
	
	@Column(name = "execution_status")
	@Enumerated(EnumType.STRING)
	private ExecutionStatus executionStatus = ExecutionStatus.RUNNING;
	
	@Column(name = "execution_output", columnDefinition = "text")
	private String executionOutput;
	
	@Column(name = "executor_execution_status")
	@Enumerated(EnumType.STRING)
	private ExecutionStatus executorExecutionStatus;
	
	@Column(name = "upload_metadata", nullable = false)
	private Boolean uploadMetadata;
	
	public ModelResultMetadata() {}

	public ModelResultMetadata(ModelResultMetadataBuilder builder) {
		this.executionStatus = builder.executionStatus;
		this.computationalModel = builder.computationalModel;
		this.modelExecutor = builder.modelExecutor;
		this.extractorMetadatas = builder.extractorMetadatas;
		this.executionMetadataFileId = builder.executionMetadataFileId;
		this.abortMetadataFileId = builder.abortMetadataFileId;
		this.userAgent = builder.userAgent;
		this.executionOutput = builder.executionOutput;
		this.executionStartDate = builder.executionStartDate;
		this.executionFinishDate = builder.executionFinishDate;
		this.executionEnvironment = builder.executionEnvironment;
		this.executorExecutionStatus = builder.executorExecutionStatus;
		this.uploadMetadata = builder.uploadMetadata;
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
	
	public ModelExecutor getModelExecutor() {
		return modelExecutor;
	}

	public void setModelExecutor(ModelExecutor modelExecutor) {
		this.modelExecutor = modelExecutor;
	}

	public Set<ExtractorMetadata> getExtractorMetadatas() {
		return extractorMetadatas;
	}

	public void setExtractorMetadatas(Set<ExtractorMetadata> extractorMetadatas) {
		this.extractorMetadatas = extractorMetadatas;
	}
	
	public String getExecutionMetadataFileId() {
		return executionMetadataFileId;
	}

	public void setExecutionMetadataFileId(String executionMetadataFileId) {
		this.executionMetadataFileId = executionMetadataFileId;
	}

	public String getAbortMetadataFileId() {
		return abortMetadataFileId;
	}

	public void setAbortMetadataFileId(String abortMetadataFileId) {
		this.abortMetadataFileId = abortMetadataFileId;
	}

	public User getUserAgent() {
		return userAgent;
	}

	public void setUserAgent(User userAgent) {
		this.userAgent = userAgent;
	}
	
	public Calendar getExecutionStartDate() {
		return executionStartDate;
	}

	public void setExecutionStartDate(Calendar executionStartDate) {
		this.executionStartDate = executionStartDate;
	}

	public Calendar getExecutionFinishDate() {
		return executionFinishDate;
	}

	public void setExecutionFinishDate(Calendar executionFinishDate) {
		this.executionFinishDate = executionFinishDate;
	}
	
	public ComputationalModel getComputationalModel() {
		return computationalModel;
	}

	public void setComputationalModel(ComputationalModel computationalModel) {
		this.computationalModel = computationalModel;
	}
	
	public ExecutionStatus getExecutorExecutionStatus() {
		return executorExecutionStatus;
	}

	public void setExecutorExecutionStatus(ExecutionStatus executorExecutionStatus) {
		this.executorExecutionStatus = executorExecutionStatus;
	}

	public Boolean getUploadMetadata() {
		return uploadMetadata;
	}

	public void setUploadMetadata(Boolean uploadMetadata) {
		this.uploadMetadata = uploadMetadata;
	}

	public ExecutionStatus getExecutionStatus() {
		return executionStatus;
	}

	public void setExecutionStatus(ExecutionStatus executionStatus) {
		this.executionStatus = executionStatus;
	}
	
	public ExecutionEnvironment getExecutionEnvironment() {
		return executionEnvironment;
	}

	public void setExecutionEnvironment(ExecutionEnvironment executionEnvironment) {
		this.executionEnvironment = executionEnvironment;
	}
	
	public String getExecutionOutput() {
		return executionOutput;
	}

	public void setExecutionOutput(String executionOutput) {
		this.executionOutput = executionOutput;
	}
	
	public static ModelResultMetadataBuilder builder() {
		return new ModelResultMetadataBuilder();
	}
	
	public static class ModelResultMetadataBuilder extends BaseApiEntityBuilder {
		
		private ModelExecutor modelExecutor;
		private Set<ExtractorMetadata> extractorMetadatas;
		private String abortMetadataFileId;
		private String executionMetadataFileId;
		private User userAgent;
		private Calendar executionStartDate;
		private Calendar executionFinishDate;
		private ComputationalModel computationalModel;
		private ExecutionEnvironment executionEnvironment;
		private ExecutionStatus executionStatus = ExecutionStatus.RUNNING;
		private String executionOutput;
		private ExecutionStatus executorExecutionStatus;
		private Boolean uploadMetadata;
		
		public ModelResultMetadataBuilder executionOutput(String executionOutput) {
			this.executionOutput = executionOutput;
			return this;
		}
		
		public ModelResultMetadataBuilder executorExecutionStatus(ExecutionStatus executorExecutionStatus) {
			this.executorExecutionStatus = executorExecutionStatus;
			return this;
		}
		
		public ModelResultMetadataBuilder uploadMetadata(Boolean uploadMetadata) {
			this.uploadMetadata = uploadMetadata;
			return this;
		}
		
		public ModelResultMetadataBuilder executionEnvironment(ExecutionEnvironment executionEnvironment) {
			this.executionEnvironment = executionEnvironment;
			return this;
		}
		
		public ModelResultMetadataBuilder executionStatus(ExecutionStatus executionStatus) {
			this.executionStatus = executionStatus;
			return this;
		}
		
		public ModelResultMetadataBuilder computationalModel(ComputationalModel computationalModel) {
			this.computationalModel = computationalModel;
			return this;
		}
		
		public ModelResultMetadataBuilder executionStartDate(Calendar executionStartDate) {
			this.executionStartDate = executionStartDate;
			return this;
		}
		
		public ModelResultMetadataBuilder executionFinishDate(Calendar executionFinishDate) {
			this.executionFinishDate = executionFinishDate;
			return this;
		}
		
		public ModelResultMetadataBuilder userAgent(User userAgent) {
			this.userAgent = userAgent;
			return this;
		}
		
		public ModelResultMetadataBuilder modelExecutor(ModelExecutor modelExecutor) {
			this.modelExecutor = modelExecutor;
			return this;
		}
		
		public ModelResultMetadataBuilder extractorMetadatas(Set<ExtractorMetadata> extractorMetadatas) {
			this.extractorMetadatas = extractorMetadatas;
			return this;
		}
		
		public ModelResultMetadataBuilder executionMetadataFileId(String executionMetadataFileId) {
			this.executionMetadataFileId = executionMetadataFileId;
			return this;
		}
		
		public ModelResultMetadataBuilder abortMetadataFileId(String abortMetadataFileId) {
			this.abortMetadataFileId = abortMetadataFileId;
			return this;
		}
		
		public ModelResultMetadata build() {
			return new ModelResultMetadata(this);
		}
	}

}