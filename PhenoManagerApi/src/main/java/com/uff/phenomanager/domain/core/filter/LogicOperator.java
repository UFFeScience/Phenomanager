package com.uff.phenomanager.domain.core.filter;

public enum LogicOperator {
	
	OR("_or_", ","), 
	AND("_and_", ";");
	
	private final String operator;
	private final String operatorAlias;

	LogicOperator(String operator, String operatorAlias) {
		this.operator = operator;
		this.operatorAlias = operatorAlias;
	}
	
	public String getOperator() {
		return operator;
	}

	public String getOperatorAlias() {
		return operatorAlias;
	}
	
	public static LogicOperator getLogicOperator(String logicalOperator) {
		if (LogicOperator.isOrOperator(logicalOperator)) {
			return LogicOperator.OR;
		
		} else if (LogicOperator.isAndOperator(logicalOperator)) {
			return LogicOperator.AND;
		}
		
		return null;
	}
	
	public static Boolean isOrOperator(String logicOperator) {
		return OR.operator.equalsIgnoreCase(logicOperator) || OR.operatorAlias.equalsIgnoreCase(logicOperator) || 
				OR.name().equalsIgnoreCase(logicOperator);
	}
	
	public static Boolean isAndOperator(String logicOperator) {
		return AND.operator.equalsIgnoreCase(logicOperator) || AND.operatorAlias.equalsIgnoreCase(logicOperator) ||
				AND.name().equalsIgnoreCase(logicOperator);
	}
	
}