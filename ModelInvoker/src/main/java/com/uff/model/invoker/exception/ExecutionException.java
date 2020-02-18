package com.uff.model.invoker.exception;

public class ExecutionException extends Exception {
	
	private static final long serialVersionUID = 1L;
	
	private String executionLog;

	public ExecutionException(String message) {
        super(message);
    }
	
	public ExecutionException(String message, String executionLog) {
        super(message);
        this.executionLog = executionLog;
    }

    public ExecutionException(String message, Throwable throwable) {
        super(message, throwable);
    }
    
    public String getExecutionLog() {
    	return executionLog;
    }
	
}