package com.uff.model.invoker.util.wrapper;

import com.uff.model.invoker.domain.ModelResultMetadata;
import com.uff.model.invoker.service.ModelResultMetadataService;

public class LogSaverWrapper {
	
	private ModelResultMetadata modelResultMetadata;
	private ModelResultMetadataService modelResultMetadataService;
	private String logOutput;
	
	public LogSaverWrapper(LogSaverWrapperBuilder builder) {
		this.modelResultMetadata = builder.modelResultMetadata;
		this.modelResultMetadataService = builder.modelResultMetadataService;
		this.logOutput = builder.logOutput;
	}
	
	public void saveLog(String executionLog) {
		if (modelResultMetadata != null && modelResultMetadataService != null) {
			modelResultMetadata = modelResultMetadataService.updateExecutorOutput(modelResultMetadata, executionLog);
		}
	}
	
	public void appendLog(String executionLog) {
		StringBuilder log = new StringBuilder();
		if (logOutput != null) {
			log.append(logOutput);
			log.append("\n");
		}
		logOutput = log.append(executionLog).toString();
	}
	
	public void updateLog(String executionLog) {
		appendLog(executionLog);
		saveLog(executionLog);
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
	
	public String getLogOutput() {
		return logOutput;
	}

	public void setLogOutput(String logOutput) {
		this.logOutput = logOutput;
	}

	public static LogSaverWrapperBuilder builder() {
		return new LogSaverWrapperBuilder();
	}
	
	public static class LogSaverWrapperBuilder {
		
		private ModelResultMetadata modelResultMetadata;
		private ModelResultMetadataService modelResultMetadataService;
		private String logOutput;
		
		public LogSaverWrapperBuilder modelResultMetadata(ModelResultMetadata modelResultMetadata) {
			this.modelResultMetadata = modelResultMetadata;
			return this;
		}
		
		public LogSaverWrapperBuilder modelResultMetadataService(ModelResultMetadataService modelResultMetadataService) {
			this.modelResultMetadataService = modelResultMetadataService;
			return this;
		}
		
		public LogSaverWrapperBuilder logOutput(String logOutput) {
			this.logOutput = logOutput;
			return this;
		}
		
		public LogSaverWrapper build() {
			return new LogSaverWrapper(this);
		}
	}
	
}