package com.uff.model.invoker.domain.dto.amqp;

import java.io.Serializable;
import java.util.Calendar;

import com.uff.model.invoker.domain.ExecutionCommand;

public class ModelExecutionMessageDto implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private String computationalModelSlug;
	private String modelExecutorSlug;
	private String modelMetadataExtractorSlug;
	private String userSlug;
	private Calendar executionDate;
	private String computationalModelVersion;
	private ExecutionCommand executionCommand;
	
	public ModelExecutionMessageDto() {}
	
	public ModelExecutionMessageDto(ModelExecutionMessageDtoBuilder builder) {
		this.computationalModelSlug = builder.computationalModelSlug;
		this.modelExecutorSlug = builder.modelExecutorSlug;
		this.modelMetadataExtractorSlug = builder.modelMetadataExtractorSlug;
		this.userSlug = builder.userSlug;
		this.executionDate = builder.executionDate;
		this.computationalModelVersion = builder.computationalModelVersion;
		this.executionCommand = builder.executionCommand;
	}
	
	public String getComputationalModelSlug() {
		return computationalModelSlug;
	}

	public void setComputationalModelSlug(String computationalModelSlug) {
		this.computationalModelSlug = computationalModelSlug;
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

	public static ModelExecutionMessageDtoBuilder builder() {
		return new ModelExecutionMessageDtoBuilder();
	}

	public static class ModelExecutionMessageDtoBuilder {
		
		private String computationalModelSlug;
		private String modelExecutorSlug;
		private String modelMetadataExtractorSlug;
		private String userSlug;
		private Calendar executionDate;
		private String computationalModelVersion;
		private ExecutionCommand executionCommand;
		
		public ModelExecutionMessageDtoBuilder computationalModelSlug(String computationalModelSlug) {
            this.computationalModelSlug = computationalModelSlug;
            return this;
        }
		
		public ModelExecutionMessageDtoBuilder modelExecutorSlug(String modelExecutorSlug) {
            this.modelExecutorSlug = modelExecutorSlug;
            return this;
        }
		
		public ModelExecutionMessageDtoBuilder modelMetadataExtractorSlug(String modelMetadataExtractorSlug) {
            this.modelMetadataExtractorSlug = modelMetadataExtractorSlug;
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
        
        public ModelExecutionMessageDto build() {
            return new ModelExecutionMessageDto(this);
        }
    }
	
}