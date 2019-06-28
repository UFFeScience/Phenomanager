package com.uff.phenomanager.domain;

public enum ExecutionStatus {
	
	SCHEDULED("Scheduled"), 
	RUNNING("Running"), 
	FINISHED("Finished"), 
	FAILURE("Failure"), 
	ABORTED("Aborted"),
	IDLE("idle");
	
	private final String executionStatusName;

	ExecutionStatus(String executionStatusName) {
		this.executionStatusName = executionStatusName;
	}
	
	public String getExecutionStatusName() {
		return executionStatusName;
	}

	public static ExecutionStatus getExecutionStatusFromString(String executionStatusName) {
		for (ExecutionStatus status : ExecutionStatus.values()) {
			if (status.name().equals(executionStatusName) || status.getExecutionStatusName().equals(executionStatusName)) {
				return status;
			}
		}
		
		return null;
	}
}
