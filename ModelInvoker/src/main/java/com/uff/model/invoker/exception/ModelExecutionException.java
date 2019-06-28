package com.uff.model.invoker.exception;

public class ModelExecutionException extends Exception {
	
	private static final long serialVersionUID = 1L;
	
	private String executionLog;

	public ModelExecutionException(String message) {
        super(message);
    }
	
	public ModelExecutionException(String message, String executionLog) {
        super(message);
        this.executionLog = executionLog;
    }

    public ModelExecutionException(String message, Throwable throwable) {
        super(message, throwable);
    }
    
    public String getExecutionLog() {
    	return executionLog;
    }
	
}