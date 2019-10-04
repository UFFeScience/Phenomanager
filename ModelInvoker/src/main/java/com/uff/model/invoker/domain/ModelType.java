package com.uff.model.invoker.domain;

public enum ModelType {
	
	WORKFLOW("workflow"), 
	EXECUTABLE("executable"), 
	COMMAND("command"), 
	WEB_SERVICE("webService");
	
	private final String typeName;

	ModelType(String typeName) {
		this.typeName = typeName;
	}
	
	public String getTypeName() {
		return typeName;
	}
	
}