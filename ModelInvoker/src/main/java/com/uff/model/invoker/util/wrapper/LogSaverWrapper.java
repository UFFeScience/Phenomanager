package com.uff.model.invoker.util.wrapper;

import com.uff.model.invoker.domain.Execution;
import com.uff.model.invoker.service.ExecutionService;

public class LogSaverWrapper {
	
	private Execution execution;
	private ExecutionService executionService;
	private String logOutput;
	
	public LogSaverWrapper(LogSaverWrapperBuilder builder) {
		this.execution = builder.execution;
		this.executionService = builder.executionService;
		this.logOutput = builder.logOutput;
	}
	
	public void saveLog(String executionLog) {
		if (execution != null && executionService != null) {
			execution = executionService.updateExecutorOutput(execution, executionLog);
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
	
	public Execution getExecution() {
		return execution;
	}

	public void setExecution(Execution execution) {
		this.execution = execution;
	}

	public ExecutionService getExecutionService() {
		return executionService;
	}

	public void setExecutionService(ExecutionService executionService) {
		this.executionService = executionService;
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
		
		private Execution execution;
		private ExecutionService executionService;
		private String logOutput;
		
		public LogSaverWrapperBuilder execution(Execution execution) {
			this.execution = execution;
			return this;
		}
		
		public LogSaverWrapperBuilder executionService(ExecutionService executionService) {
			this.executionService = executionService;
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