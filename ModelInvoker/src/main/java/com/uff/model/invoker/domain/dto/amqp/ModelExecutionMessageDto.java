package com.uff.model.invoker.domain.dto.amqp;

import java.io.Serializable;
import java.util.Calendar;
import java.util.List;

import com.uff.model.invoker.domain.ExecutionCommand;

public class ModelExecutionMessageDto implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private String modelExecutorSlug;
	private String modelMetadataExtractorSlug;
	private String modelResultMetadataSlug;
	private List<String> executionExtractors;
	private String executionEnvironmentSlug;
	private String userSlug;
	private Calendar executionDate;
	private String computationalModelVersion;
	private ExecutionCommand executionCommand;
	private Boolean uploadMetadata = Boolean.FALSE;
	
	public ModelExecutionMessageDto() {}
	
	public ModelExecutionMessageDto(ModelExecutionMessageDtoBuilder builder) {
		this.modelExecutorSlug = builder.modelExecutorSlug;
		this.modelResultMetadataSlug = builder.modelResultMetadataSlug;
		this.executionExtractors = builder.executionExtractors;
		this.modelMetadataExtractorSlug = builder.modelMetadataExtractorSlug;
		this.executionEnvironmentSlug = builder.executionEnvironmentSlug;
		this.userSlug = builder.userSlug;
		this.executionDate = builder.executionDate;
		this.computationalModelVersion = builder.computationalModelVersion;
		this.executionCommand = builder.executionCommand;
		this.uploadMetadata = builder.uploadMetadata;
	}

	public String getUserSlug() {
		return userSlug;
	}

	public void setUserSlug(String userSlug) {
		this.userSlug = userSlug;
	}
	
	public List<String> getExecutionExtractors() {
		return executionExtractors;
	}

	public void setExecutionExtractors(List<String> executionExtractors) {
		this.executionExtractors = executionExtractors;
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
	
	public String getModelExecutorSlug() {
		return modelExecutorSlug;
	}

	public void setModelExecutorSlug(String modelExecutorSlug) {
		this.modelExecutorSlug = modelExecutorSlug;
	}

	public String getModelMetadataExtractorSlug() {
		return modelMetadataExtractorSlug;
	}

	public void setModelMetadataExtractorSlug(String modelExtractorSlug) {
		this.modelMetadataExtractorSlug = modelExtractorSlug;
	}
	
	public String getExecutionEnvironmentSlug() {
		return executionEnvironmentSlug;
	}

	public void setExecutionEnvironmentSlug(String executionEnvironmentSlug) {
		this.executionEnvironmentSlug = executionEnvironmentSlug;
	}
	
	public Boolean getUploadMetadata() {
		return uploadMetadata;
	}

	public void setUploadMetadata(Boolean uploadMetadata) {
		this.uploadMetadata = uploadMetadata;
	}
	
	public String getModelResultMetadataSlug() {
		return modelResultMetadataSlug;
	}

	public void setModelResultMetadataSlug(String modelResultMetadataSlug) {
		this.modelResultMetadataSlug = modelResultMetadataSlug;
	}

	public static ModelExecutionMessageDtoBuilder builder() {
		return new ModelExecutionMessageDtoBuilder();
	}

	public static class ModelExecutionMessageDtoBuilder {
		
		private String modelExecutorSlug;
		private String modelMetadataExtractorSlug;
		private String modelResultMetadataSlug;
		private List<String> executionExtractors;
		private String executionEnvironmentSlug;
		private String userSlug;
		private Calendar executionDate;
		private String computationalModelVersion;
		private ExecutionCommand executionCommand;
		private Boolean uploadMetadata = Boolean.FALSE;
		
		public ModelExecutionMessageDtoBuilder modelExecutorSlug(String modelExecutorSlug) {
            this.modelExecutorSlug = modelExecutorSlug;
            return this;
        }
		
		public ModelExecutionMessageDtoBuilder modelResultMetadataSlug(String modelResultMetadataSlug) {
            this.modelResultMetadataSlug = modelResultMetadataSlug;
            return this;
        }
		
		public ModelExecutionMessageDtoBuilder executionExtractors(List<String> executionExtractors) {
            this.executionExtractors = executionExtractors;
            return this;
        }
		
		public ModelExecutionMessageDtoBuilder modelMetadataExtractorSlug(String modelMetadataExtractorSlug) {
            this.modelMetadataExtractorSlug = modelMetadataExtractorSlug;
            return this;
        }
		
		public ModelExecutionMessageDtoBuilder executionEnvironmentSlug(String executionEnvironmentSlug) {
            this.executionEnvironmentSlug = executionEnvironmentSlug;
            return this;
		}

        public ModelExecutionMessageDtoBuilder userSlug(String userSlug) {
            this.userSlug = userSlug;
            return this;
        }

        public ModelExecutionMessageDtoBuilder executionDate(Calendar executionDate) {
            this.executionDate = executionDate;
            return this;
        }
        
        public ModelExecutionMessageDtoBuilder computationalModelVersion(String computationalModelVersion) {
            this.computationalModelVersion = computationalModelVersion;
            return this;
        }
        
        public ModelExecutionMessageDtoBuilder executionCommand(ExecutionCommand executionCommand) {
            this.executionCommand = executionCommand;
            return this;
        }
        
        public ModelExecutionMessageDtoBuilder uploadMetadata(Boolean uploadMetadata) {
            this.uploadMetadata = uploadMetadata;
            return this;
        }
        
        public ModelExecutionMessageDto build() {
            return new ModelExecutionMessageDto(this);
        }
    }

	@Override
	public String toString() {
		return "ModelExecutionMessageDto [modelExecutorSlug=" + modelExecutorSlug + ", modelMetadataExtractorSlug="
				+ modelMetadataExtractorSlug + ", modelResultMetadataSlug=" + modelResultMetadataSlug
				+ ", executionExtractors=" + executionExtractors + ", executionEnvironmentSlug="
				+ executionEnvironmentSlug + ", userSlug=" + userSlug + ", executionDate=" + executionDate
				+ ", computationalModelVersion=" + computationalModelVersion + ", executionCommand=" + executionCommand
				+ ", uploadMetadata=" + uploadMetadata + "]";
	}
	
}