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
import com.uff.phenomanager.domain.core.BaseApiEntity;

@Entity
@Table(name = "execution")
public class Execution extends BaseApiEntity {
	
	@ManyToOne
	@Cascade(CascadeType.SAVE_UPDATE)
	@JoinColumn(name = "id_executor")
	private Executor executor;
	
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "execution")
	@Cascade(CascadeType.DELETE)
	@JsonManagedReference(value = "extractorExecutions")
	private Set<ExtractorExecution> extractorExecutions;
	
	@Column(name = "execution_metadata_file_id")
	private String executionMetadataFileId;
	
	@Column(name = "abortion_metadata_file_id")
	private String abortionMetadataFileId;
	
	@ManyToOne
	@Cascade(CascadeType.SAVE_UPDATE)
	@JoinColumn(name = "id_computational_model")
	private ComputationalModel computationalModel;
	
	@ManyToOne
	@Cascade(CascadeType.SAVE_UPDATE)
	@JoinColumn(name = "id_environment")
	private Environment environment;
	
	@ManyToOne
	@Cascade(CascadeType.SAVE_UPDATE)
	@JoinColumn(name = "id_user_account_agent", referencedColumnName = "id")
	private User userAgent;
	
	@Column(name = "start_date")
	private Calendar startDate;
	
	@Column(name = "finish_date")
	private Calendar finishDate;
	
	@Column(name = "status")
	@Enumerated(EnumType.STRING)
	private ExecutionStatus status = ExecutionStatus.RUNNING;
	
	@Column(name = "output", columnDefinition = "text")
	private String output;
	
	@Column(name = "executor_status")
	@Enumerated(EnumType.STRING)
	private ExecutionStatus executorStatus;
	
	@Column(name = "upload_metadata", nullable = false)
	private Boolean uploadMetadata;
	
	@Column(name = "has_abortion_requested")
	private Boolean hasAbortionRequested;
	
	public Execution() {}

	public Execution(ExecutionBuilder builder) {
		this.status = builder.status;
		this.computationalModel = builder.computationalModel;
		this.executor = builder.executor;
		this.hasAbortionRequested = builder.hasAbortionRequested;
		this.extractorExecutions = builder.extractorExecution;
		this.executionMetadataFileId = builder.executionMetadataFileId;
		this.abortionMetadataFileId = builder.abortionMetadataFileId;
		this.userAgent = builder.userAgent;
		this.output = builder.output;
		this.startDate = builder.startDate;
		this.finishDate = builder.finishDate;
		this.environment = builder.environment;
		this.executorStatus = builder.executorStatus;
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
	}
	
	public Executor getExecutor() {
		return executor;
	}

	public void setExecutor(Executor executor) {
		this.executor = executor;
	}

	public Set<ExtractorExecution> getExtractorExecutions() {
		return extractorExecutions;
	}

	public void setExtractorExecutions(Set<ExtractorExecution> extractorExecutions) {
		this.extractorExecutions = extractorExecutions;
	}
	
	public String getExecutionMetadataFileId() {
		return executionMetadataFileId;
	}

	public void setExecutionMetadataFileId(String executionMetadataFileId) {
		this.executionMetadataFileId = executionMetadataFileId;
	}

	public String getAbortionMetadataFileId() {
		return abortionMetadataFileId;
	}

	public void setAbortionMetadataFileId(String abortionMetadataFileId) {
		this.abortionMetadataFileId = abortionMetadataFileId;
	}

	public User getUserAgent() {
		return userAgent;
	}

	public void setUserAgent(User userAgent) {
		this.userAgent = userAgent;
	}
	
	public Calendar getStartDate() {
		return startDate;
	}

	public void setStartDate(Calendar startDate) {
		this.startDate = startDate;
	}

	public Calendar getFinishDate() {
		return finishDate;
	}

	public void setFinishDate(Calendar finishDate) {
		this.finishDate = finishDate;
	}
	
	public ComputationalModel getComputationalModel() {
		return computationalModel;
	}

	public void setComputationalModel(ComputationalModel computationalModel) {
		this.computationalModel = computationalModel;
	}
	
	public ExecutionStatus getExecutorStatus() {
		return executorStatus;
	}

	public void setExecutorStatus(ExecutionStatus executorStatus) {
		this.executorStatus = executorStatus;
	}

	public Boolean getUploadMetadata() {
		return uploadMetadata;
	}

	public void setUploadMetadata(Boolean uploadMetadata) {
		this.uploadMetadata = uploadMetadata;
	}

	public ExecutionStatus getStatus() {
		return status;
	}

	public void setStatus(ExecutionStatus status) {
		this.status = status;
	}
	
	public Boolean getHasAbortRequested() {
		return hasAbortionRequested;
	}

	public void setHasAbortRequested(Boolean hasAbortRequested) {
		this.hasAbortionRequested = hasAbortRequested;
	}

	public Environment getEnvironment() {
		return environment;
	}

	public void setEnvironment(Environment environment) {
		this.environment = environment;
	}
	
	public String getOutput() {
		return output;
	}

	public void setOutput(String output) {
		this.output = output;
	}
	
	public static ExecutionBuilder builder() {
		return new ExecutionBuilder();
	}
	
	public static class ExecutionBuilder extends BaseApiEntityBuilder {
		
		private Executor executor;
		private Set<ExtractorExecution> extractorExecution;
		private String abortionMetadataFileId;
		private String executionMetadataFileId;
		private User userAgent;
		private Calendar startDate;
		private Calendar finishDate;
		private ComputationalModel computationalModel;
		private Environment environment;
		private ExecutionStatus status = ExecutionStatus.RUNNING;
		private String output;
		private Boolean hasAbortionRequested;
		private ExecutionStatus executorStatus;
		private Boolean uploadMetadata;
		
		public ExecutionBuilder output(String output) {
			this.output = output;
			return this;
		}
		
		public ExecutionBuilder hasAbortionRequested(Boolean hasAbortionRequested) {
			this.hasAbortionRequested = hasAbortionRequested;
			return this;
		}
		
		public ExecutionBuilder executorStatus(ExecutionStatus executorStatus) {
			this.executorStatus = executorStatus;
			return this;
		}
		
		public ExecutionBuilder uploadMetadata(Boolean uploadMetadata) {
			this.uploadMetadata = uploadMetadata;
			return this;
		}
		
		public ExecutionBuilder environment(Environment environment) {
			this.environment = environment;
			return this;
		}
		
		public ExecutionBuilder status(ExecutionStatus status) {
			this.status = status;
			return this;
		}
		
		public ExecutionBuilder computationalModel(ComputationalModel computationalModel) {
			this.computationalModel = computationalModel;
			return this;
		}
		
		public ExecutionBuilder startDate(Calendar startDate) {
			this.startDate = startDate;
			return this;
		}
		
		public ExecutionBuilder finishDate(Calendar finishDate) {
			this.finishDate = finishDate;
			return this;
		}
		
		public ExecutionBuilder userAgent(User userAgent) {
			this.userAgent = userAgent;
			return this;
		}
		
		public ExecutionBuilder executor(Executor executor) {
			this.executor = executor;
			return this;
		}
		
		public ExecutionBuilder extractorExecutions(Set<ExtractorExecution> extractorExecutions) {
			this.extractorExecution = extractorExecutions;
			return this;
		}
		
		public ExecutionBuilder executionMetadataFileId(String executionMetadataFileId) {
			this.executionMetadataFileId = executionMetadataFileId;
			return this;
		}
		
		public ExecutionBuilder abortionMetadataFileId(String abortionMetadataFileId) {
			this.abortionMetadataFileId = abortionMetadataFileId;
			return this;
		}
		
		public Execution build() {
			return new Execution(this);
		}
	}

}