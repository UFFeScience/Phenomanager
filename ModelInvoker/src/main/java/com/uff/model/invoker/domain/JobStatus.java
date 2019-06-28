package com.uff.model.invoker.domain;

public enum JobStatus {
	
	CANCELLED("CA"), 
	COMPLETED("CD"),
	COMPLETING("CG"),
	FAILED("F"),
	NODE_FAIL("NF"),
	PENDING("PD"),
	RUNNING("R"),
	TIMEOUT("TO");
	
	private final String status;

	JobStatus(String status) {
		this.status = status;
	}
	
	public String getStatus() {
		return status;
	}
	
	public static JobStatus getJobStatusFromString(String status) {
		for (JobStatus jobStatus : JobStatus.values()) {
			if (jobStatus.name().equals(status) || jobStatus.getStatus().equals(status)) {
				return jobStatus;
			}
		}
		
		return null;
	}
	
}