package com.uff.phenomanager.domain.dto.amqp;

import java.io.Serializable;
import java.util.Calendar;
import java.util.List;

import com.uff.phenomanager.domain.ExecutionCommand;

public class ExecutionMessageDto implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private String executorSlug;
	private String extractorSlug;
	private String executionSlug;
	private List<String> executionExtractorSlugs;
	private String environmentSlug;
	private String userSlug;
	private Calendar executionDate;
	private String computationalModelVersion;
	private ExecutionCommand executionCommand;
	private Boolean uploadMetadata = Boolean.FALSE;
	
	public ExecutionMessageDto() {}
	
	public ExecutionMessageDto(ExecutionMessageDtoBuilder builder) {
		this.executorSlug = builder.executorSlug;
		this.executionSlug = builder.executionSlug;
		this.executionExtractorSlugs = builder.executionExtractorSlugs;
		this.extractorSlug = builder.extractorSlug;
		this.environmentSlug = builder.environmentSlug;
		this.userSlug = builder.userSlug;
		this.executionDate = builder.executionDate;
		this.computationalModelVersion = builder.computationalModelVersion;
		this.executionCommand = builder.executionCommand;
		this.uploadMetadata = builder.uploadMetadata;
	}
	
	public String getExecutorSlug() {
		return executorSlug;
	}

	public void setExecutorSlug(String executorSlug) {
		this.executorSlug = executorSlug;
	}

	public String getExtractorSlug() {
		return extractorSlug;
	}

	public void setExtractorSlug(String extractorSlug) {
		this.extractorSlug = extractorSlug;
	}

	public String getExecutionSlug() {
		return executionSlug;
	}

	public void setExecutionSlug(String executionSlug) {
		this.executionSlug = executionSlug;
	}

	public List<String> getExecutionExtractorsSlugs() {
		return executionExtractorSlugs;
	}

	public void setExecutionExtractorSlugs(List<String> executionExtractorSlugs) {
		this.executionExtractorSlugs = executionExtractorSlugs;
	}

	public String getEnvironmentSlug() {
		return environmentSlug;
	}

	public void setEnvironmentSlug(String environmentSlug) {
		this.environmentSlug = environmentSlug;
	}

	public String getUserSlug() {
		return userSlug;
	}

	public void setUserSlug(String userSlug) {
		this.userSlug = userSlug;
	}

	public Calendar getExecutionDate() {
		return executionDate;
	}

	public void setExecutionDate(Calendar executionDate) {
		this.executionDate = executionDate;
	}

	public String getComputationalModelVersion() {
		return computationalModelVersion;
	}

	public void setComputationalModelVersion(String computationalModelVersion) {
		this.computationalModelVersion = computationalModelVersion;
	}

	public ExecutionCommand getExecutionCommand() {
		return executionCommand;
	}

	public void setExecutionCommand(ExecutionCommand executionCommand) {
		this.executionCommand = executionCommand;
	}

	public Boolean getUploadMetadata() {
		return uploadMetadata;
	}

	public void setUploadMetadata(Boolean uploadMetadata) {
		this.uploadMetadata = uploadMetadata;
	}

	public static ExecutionMessageDtoBuilder builder() {
		return new ExecutionMessageDtoBuilder();
	}

	public static class ExecutionMessageDtoBuilder {
		
		private String executorSlug;
		private String extractorSlug;
		private String executionSlug;
		private List<String> executionExtractorSlugs;
		private String environmentSlug;
		private String userSlug;
		private Calendar executionDate;
		private String computationalModelVersion;
		private ExecutionCommand executionCommand;
		private Boolean uploadMetadata = Boolean.FALSE;
		
		public ExecutionMessageDtoBuilder executorSlug(String executorSlug) {
            this.executorSlug = executorSlug;
            return this;
        }
		
		public ExecutionMessageDtoBuilder executionSlug(String executionSlug) {
            this.executionSlug = executionSlug;
            return this;
        }
		
		public ExecutionMessageDtoBuilder executionExtractorSlugs(List<String> executionExtractorSlugs) {
            this.executionExtractorSlugs = executionExtractorSlugs;
            return this;
        }
		
		public ExecutionMessageDtoBuilder extractorSlug(String extractorSlug) {
            this.extractorSlug = extractorSlug;
            return this;
        }
		
		public ExecutionMessageDtoBuilder environmentSlug(String environmentSlug) {
            this.environmentSlug = environmentSlug;
            return this;
		}

        public ExecutionMessageDtoBuilder userSlug(String userSlug) {
            this.userSlug = userSlug;
            return this;
        }

        public ExecutionMessageDtoBuilder executionDate(Calendar executionDate) {
            this.executionDate = executionDate;
            return this;
        }
        
        public ExecutionMessageDtoBuilder computationalModelVersion(String computationalModelVersion) {
            this.computationalModelVersion = computationalModelVersion;
            return this;
        }
        
        public ExecutionMessageDtoBuilder executionCommand(ExecutionCommand executionCommand) {
            this.executionCommand = executionCommand;
            return this;
        }
        
        public ExecutionMessageDtoBuilder uploadMetadata(Boolean uploadMetadata) {
            this.uploadMetadata = uploadMetadata;
            return this;
        }
        
        public ExecutionMessageDto build() {
            return new ExecutionMessageDto(this);
        }
    }

	@Override
	public String toString() {
		return "ExecutionMessageDto [executorSlug=" + executorSlug + ", extractorSlug=" + extractorSlug
				+ ", executionSlug=" + executionSlug + ", executionExtractorSlugs=" + executionExtractorSlugs
				+ ", environmentSlug=" + environmentSlug + ", userSlug=" + userSlug + ", executionDate=" + executionDate
				+ ", computationalModelVersion=" + computationalModelVersion + ", executionCommand=" + executionCommand
				+ ", uploadMetadata=" + uploadMetadata + "]";
	}
	
}