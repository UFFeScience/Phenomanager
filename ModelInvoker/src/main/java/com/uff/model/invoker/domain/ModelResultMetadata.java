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
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import com.uff.model.invoker.Constants;

@Entity
@Table(name = "model_result_metadata")
public class ModelResultMetadata extends BaseApiEntity {
	
	@ManyToOne
	@Cascade(CascadeType.SAVE_UPDATE)
	@JoinColumn(name = "id_model_executor")
	private ModelExecutor modelExecutor;
	
	@OneToMany(mappedBy = "modelResultMetadata")
	@Cascade(CascadeType.DELETE)
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
	
	@Transient
	private String systemLog;
	
	public ModelResultMetadata() {}

	public ModelResultMetadata(ModelResultMetadataBuilder builder) {
		this.executionEnvironment = builder.executionEnvironment;
		this.executionStatus = builder.executionStatus;
		this.computationalModel = builder.computationalModel;
		this.modelExecutor = builder.modelExecutor;
		this.extractorMetadatas = builder.extractorMetadatas;
		this.executionMetadataFileId = builder.executionMetadataFileId;
		this.abortMetadataFileId = builder.abortMetadataFileId;
		this.executionStartDate = builder.executionStartDate;
		this.executionFinishDate = builder.executionFinishDate;
		this.userAgent = builder.userAgent;
		this.executionOutput = builder.executionOutput;
		this.setId(builder.getId());
		this.setSlug(builder.getSlug());
		this.setActive(builder.getActive());
		this.setInsertDate(builder.getInsertDate());
		this.setUpdateDate(builder.getUpdateDate());
		this.setDeleteDate(builder.getRemoveDate());
	}
	
	public ModelExecutor getModelExecutor() {
		return modelExecutor;
	}

	public void setModelExecutor(ModelExecutor modelExecutor) {
		this.modelExecutor = modelExecutor;
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

	public Set<ExtractorMetadata> getExtractorMetadatas() {
		if (extractorMetadatas == null) {
			extractorMetadatas = new HashSet<>();
		}
		
		return extractorMetadatas;
	}

	public void setExtractorMetadatas(Set<ExtractorMetadata> extractorMetadatas) {
		this.extractorMetadatas = extractorMetadatas;
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

	public String getSystemLog() {
		return systemLog;
	}

	public void setSystemLog(String systemLog) {
		this.systemLog = systemLog;
	}
	
	public void appendExecutionLog(String executionLog) {
		StringBuilder log;
		
		if (systemLog != null) {
			log = new StringBuilder(systemLog);
			log.append("\n");
		} else {
			log = new StringBuilder();
		}
		
		log.append(executionOutput);
		log.append("\n");
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
			
			executionOutput = finalOutput.toString();
			
		} else {
			executionOutput = appendedOutput;
		}
	}
	
	public void appendSystemLog(String executionLog) {
		Date date = Calendar.getInstance().getTime();  
        DateFormat dateFormat = new SimpleDateFormat("dd/mm/yyyy - hh:mm:ss");  
        String logDate = dateFormat.format(date);  
        
		if (systemLog == null) {
			StringBuilder log = new StringBuilder(logDate);
			log.append(" - ");
			log.append(executionLog);
			
			systemLog = log.toString();
			executionOutput = systemLog;
			
		} else {
			StringBuilder log = new StringBuilder(systemLog);
			log.append("\n");
			log.append(logDate);
			log.append(" - ");
			log.append(executionLog);

			systemLog = log.toString();
			executionOutput = systemLog;
		}
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
		
		public ModelResultMetadataBuilder executionOutput(String executionOutput) {
			this.executionOutput = executionOutput;
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