package com.uff.phenomanager.domain;

public enum HypothesisState {
	
	FORMULATED("Formulated"), 
	VALIDATED("Validated"), 
	CONFIRMED("Confirmed"), 
	IMPROVED("Improved"),
	REFUTED("Refuted");
	
	private final String hypothesisStatusName;

	HypothesisState(String hypothesisStatusName) {
		this.hypothesisStatusName = hypothesisStatusName;
	}
	
	public String getHypothesisStatusName() {
		return hypothesisStatusName;
	}

	public static HypothesisState getExecutionStatusFromString(String hypothesisStatusName) {
		for (HypothesisState status : HypothesisState.values()) {
			if (status.name().equals(hypothesisStatusName) || status.getHypothesisStatusName().equals(hypothesisStatusName)) {
				return status;
			}
		}
		
		return null;
	}
}
