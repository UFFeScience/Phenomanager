package com.uff.model.invoker.wrapper;

import com.uff.model.invoker.domain.ModelResultMetadata;
import com.uff.model.invoker.service.ModelResultMetadataService;

public class LogSaverWrapper {
	
	private ModelResultMetadata modelResultMetadata;
	private ModelResultMetadataService modelResultMetadataService;
	
	public LogSaverWrapper(LogSaverWrapperBuilder builder) {
		this.modelResultMetadata = builder.modelResultMetadata;
		this.modelResultMetadataService = builder.modelResultMetadataService;
	}
	
	public void saveLog() {
		if (modelResultMetadata != null && modelResultMetadataService != null) {
			modelResultMetadataService.save(modelResultMetadata);
		}
	}
	
	public void appendLog(String executionLog) {
		if (modelResultMetadata != null) {
			modelResultMetadata.appendExecutionLog(executionLog);
		}
	}
	
	public void updateLog(String executionLog) {
		appendLog(executionLog);
		saveLog();
	}
	
	public ModelResultMetadata getModelResultMetadata() {
		return modelResultMetadata;
	}

	public void setModelResultMetadata(ModelResultMetadata modelResultMetadata) {
		this.modelResultMetadata = modelResultMetadata;
	}

	public ModelResultMetadataService getModelResultMetadataService() {
		return modelResultMetadataService;
	}

	public void setModelResultMetadataService(ModelResultMetadataService modelResultMetadataService) {
		this.modelResultMetadataService = modelResultMetadataService;
	}

	public static LogSaverWrapperBuilder builder() {
		return new LogSaverWrapperBuilder();
	}
	
	public static class LogSaverWrapperBuilder {
		
		private ModelResultMetadata modelResultMetadata;
		private ModelResultMetadataService modelResultMetadataService;
		
		public LogSaverWrapperBuilder modelResultMetadata(ModelResultMetadata modelResultMetadata) {
			this.modelResultMetadata = modelResultMetadata;
			return this;
		}
		
		public LogSaverWrapperBuilder modelResultMetadataService(ModelResultMetadataService modelResultMetadataService) {
			this.modelResultMetadataService = modelResultMetadataService;
			return this;
		}
		
		public LogSaverWrapper build() {
			return new LogSaverWrapper(this);
		}
	}
	
}