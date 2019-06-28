package com.uff.phenomanager.domain.core.filter;

public enum FilterOperator {
	
	EQ("=eq=",  "=", "|eq|"), 
	LE("=le=", "<=", "|le|"), 
	GE("=ge=", ">=", "|ge|"), 
	GT("=gt=", ">", "|gt|"), 
	LT("=lt=", "<", "|lt|"), 
	NE("=ne=", "!=", "|ne|"),
	IN("=in=", "=in=", "|in|"),
	OU("=out=", "=out=", "|ou|"),
	LK("=like=", "=like=", "|lk|");
	
	private final String operatorAlias;
	private final String operatorCommonAlias;
	private final String parseableOperator;

	FilterOperator(String operatorAlias, String operatorCommonAlias, String parseableOperator) {
		this.operatorAlias = operatorAlias;
		this.operatorCommonAlias = operatorCommonAlias;
		this.parseableOperator = parseableOperator;
	}
	
	public static FilterOperator getFilterOperator(String operator) {
		for (FilterOperator filterOperator : FilterOperator.values()) {
			if (filterOperator.name().equalsIgnoreCase(operator) || filterOperator.getOperatorAlias().equalsIgnoreCase(operator)
				|| filterOperator.getOperatorCommonAlias().equalsIgnoreCase(operator)
				|| filterOperator.getParseableOperator().equalsIgnoreCase(operator)) {
				
				return filterOperator;
			}
		}
		
		return null;
	}
	
	public String getOperatorAlias() {
		return operatorAlias;
	}
	
	public String getOperatorCommonAlias() {
		return operatorCommonAlias;
	}

	public String getParseableOperator() {
		return parseableOperator;
	}
	
}