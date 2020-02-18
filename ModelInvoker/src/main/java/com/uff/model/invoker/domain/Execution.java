package com.uff.model.invoker.domain;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
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

import com.uff.model.invoker.Constants;
import com.uff.model.invoker.domain.core.BaseApiEntity;

@Entity
@Table(name = "execution")
public class Execution extends BaseApiEntity {
	
	@ManyToOne
	@Cascade(CascadeType.SAVE_UPDATE)
	@JoinColumn(name = "id_executor")
	private Executor executor;
	
	@OneToMany(mappedBy = "execution", fetch = FetchType.EAGER )
	@Cascade(CascadeType.DELETE)
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
	
	@Column(name = "executor_status")
	@Enumerated(EnumType.STRING)
	private ExecutionStatus executorStatus;
	
	@Column(name = "upload_metadata", nullable = false)
	private Boolean uploadMetadata;
	
	@Column(name = "has_abortion_requested")
	private Boolean hasAbortionRequested;
	
	@Column(name = "output", columnDefinition = "text")
	private String output;
	
	public Execution() {}

	public Execution(ExecutionBuilder builder) {
		this.environment = builder.environment;
		this.uploadMetadata = builder.uploadMetadata;
		this.hasAbortionRequested = builder.hasAbortionRequested;
		this.status = builder.status;
		this.computationalModel = builder.computationalModel;
		this.executor = builder.executor;
		this.extractorExecutions = builder.extractorExecutions;
		this.executionMetadataFileId = builder.executionMetadataFileId;
		this.abortionMetadataFileId = builder.abortionMetadataFileId;
		this.startDate = builder.startDate;
		this.finishDate = builder.finishDate;
		this.userAgent = builder.userAgent;
		this.output = builder.output;
		this.executorStatus = builder.executorStatus;
		this.setId(builder.getId());
		this.setSlug(builder.getSlug());
		this.setActive(builder.getActive());
		this.setInsertDate(builder.getInsertDate());
		this.setUpdateDate(builder.getUpdateDate());
		this.setDeleteDate(builder.getRemoveDate());
	}
	
	public Executor getExecutor() {
		return executor;
	}

	public void setExecutor(Executor executor) {
		this.executor = executor;
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
	
	public Boolean getUploadMetadata() {
		return uploadMetadata;
	}

	public void setUploadMetadata(Boolean uploadMetadata) {
		this.uploadMetadata = uploadMetadata;
	}

	public Set<ExtractorExecution> getExtractorExecutions() {
		if (extractorExecutions == null) {
			extractorExecutions = new HashSet<>();
		}
		
		return extractorExecutions;
	}

	public void setExtractorExecutions(Set<ExtractorExecution> extractorExecutions) {
		this.extractorExecutions = extractorExecutions;
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
	
	public ExecutionStatus getStatus() {
		return status;
	}

	public void setStatus(ExecutionStatus status) {
		this.status = status;
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

	public ExecutionStatus getExecutorStatus() {
		return executorStatus;
	}

	public void setExecutorStatus(ExecutionStatus executorStatus) {
		this.executorStatus = executorStatus;
	}
	
	public Boolean getHasAbortionRequested() {
		return hasAbortionRequested;
	}

	public void setHasAbortionRequested(Boolean hasAbortionRequested) {
		this.hasAbortionRequested = hasAbortionRequested;
	}

	public void appendExecutionLog(String executionLog) {
		StringBuilder log = new StringBuilder();
		
		if (output != null) {
			log.append(output);
			log.append("\n");
		}

		log.append(executionLog);
		
		String appendedOutput = log.toString();
		String[] splittedOutput = appendedOutput.split("\n");
				
		if (splittedOutput.length > Constants.MAX_LOG_LINES) {
			StringBuilder finalOutput = new StringBuilder();
			Integer initialIndex = splittedOutput.length - Constants.MAX_LOG_LINES;
			
			for (; initialIndex < splittedOutput.length; initialIndex++) {
				finalOutput.append(splittedOutput[initialIndex]);
				
				if (initialIndex < (splittedOutput.length - 1)) {
					finalOutput.append("\n");
				}
			}
			
			output = finalOutput.toString();
			
		} else {
			output = appendedOutput;
		}
	}
	
	public void appendSystemLogs(String[] executionLogs) {
		if (executionLogs != null && executionLogs.length > 0) {
			for (int i = 0; i < executionLogs.length; i++) {
				appendSystemLog(executionLogs[i]);
			}
		}
	}
	
	public void appendSystemLog(String executionLog) {
		StringBuilder log = new StringBuilder();
		
		if (output != null) {
			log.append(output);
			log.append("\n");
		}
		
		Date date = Calendar.getInstance().getTime();  
        DateFormat dateFormat = new SimpleDateFormat("dd/mm/yyyy - hh:mm:ss");  
        String logDate = dateFormat.format(date); 
        
        log.append(logDate);
		log.append(" - ");
		log.append(executionLog);
		
		String appendedOutput = log.toString();
		String[] splittedOutput = appendedOutput.split("\n");
				
		if (splittedOutput.length > Constants.MAX_LOG_LINES) {
			StringBuilder finalOutput = new StringBuilder();
			Integer initialIndex = splittedOutput.length - Constants.MAX_LOG_LINES;
			
			for (; initialIndex < splittedOutput.length; initialIndex++) {
				finalOutput.append(splittedOutput[initialIndex]);
				
				if (initialIndex < (splittedOutput.length - 1)) {
					finalOutput.append("\n");
				}
			}
			
			output = finalOutput.toString();
			
		} else {
			output = appendedOutput;
		}
	}
	
	public static ExecutionBuilder builder() {
		return new ExecutionBuilder();
	}
	
	public static class ExecutionBuilder extends BaseApiEntityBuilder {
		
		private Executor executor;
		private Set<ExtractorExecution> extractorExecutions;
		private String abortionMetadataFileId;
		private String executionMetadataFileId;
		private Boolean uploadMetadata;
		private Boolean hasAbortionRequested;
		private User userAgent;
		private Calendar startDate;
		private Calendar finishDate;
		private ComputationalModel computationalModel;
		private Environment environment;
		private ExecutionStatus status = ExecutionStatus.RUNNING;
		private String output;
		private ExecutionStatus executorStatus;
		
		public ExecutionBuilder output(String output) {
			this.output = output;
			return this;
		}
		
		public ExecutionBuilder hasAbortionRequested(Boolean hasAbortionRequested) {
			this.hasAbortionRequested = hasAbortionRequested;
			return this;
		}
		
		public ExecutionBuilder uploadMetadata(Boolean uploadMetadata) {
			this.uploadMetadata = uploadMetadata;
			return this;
		}
		
		public ExecutionBuilder executorStatus(ExecutionStatus executorStatus) {
			this.executorStatus = executorStatus;
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
			this.extractorExecutions = extractorExecutions;
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