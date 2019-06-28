package com.uff.phenomanager.domain.core.filter;

public enum AggregateFunction {
	
	SUM("sum"), 
	AVG("avg"), 
	COUNT("count"), 
	COUNT_DISTINCT("count_distinct");
	
	private final String function;

	AggregateFunction(String function) {
		this.function = function;
	}
	
	public String getFunction() {
		return function;
	}
	
	public static AggregateFunction getAggregateFunction(String function) {
		for (AggregateFunction aggregateFunction : AggregateFunction.values()) {
			if (aggregateFunction.name().equalsIgnoreCase(function) || 
					aggregateFunction.getFunction().equalsIgnoreCase(function)) {
				return aggregateFunction;
			}
		}
		
		return null;
	}
	
	@SuppressWarnings("unlikely-arg-type")
	public static Boolean isSumFunction(String aggregateFunction) {
		return SUM.equals(aggregateFunction) || SUM.name().equals(aggregateFunction) || 
				SUM.getFunction().equals(aggregateFunction);
	}
	
	@SuppressWarnings("unlikely-arg-type")
	public static Boolean isAvgFunction(String aggregateFunction) {
		return AVG.equals(aggregateFunction) || AVG.name().equals(aggregateFunction) || 
				AVG.getFunction().equals(aggregateFunction);
	}
	
	@SuppressWarnings("unlikely-arg-type")
	public static Boolean isCountFunction(String aggregateFunction) {
		return COUNT.equals(aggregateFunction) || COUNT.name().equals(aggregateFunction) || 
				COUNT.getFunction().equals(aggregateFunction);
	}
	
	@SuppressWarnings("unlikely-arg-type")
	public static Boolean isCountDistinctFunction(String aggregateFunction) {
		return COUNT_DISTINCT.equals(aggregateFunction) || COUNT_DISTINCT.name().equals(aggregateFunction) || 
				COUNT_DISTINCT.getFunction().equals(aggregateFunction);
	}
	
}