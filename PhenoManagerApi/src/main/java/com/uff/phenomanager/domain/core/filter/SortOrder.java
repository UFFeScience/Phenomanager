package com.uff.phenomanager.domain.core.filter;

public enum SortOrder {
	
	ASC("asc"), 
	DESC("desc");
	
	private final String order;

	SortOrder(String order) {
		this.order = order;
	}
	
	public String getOrder() {
		return order;
	}
	
	public static SortOrder getSortOrder(String order) {
		for (SortOrder sortOrder : SortOrder.values()) {
			if (sortOrder.name().equalsIgnoreCase(order) || sortOrder.getOrder().equalsIgnoreCase(order)) {
				return sortOrder;
			}
		}
		
		return null;
	}
	
}