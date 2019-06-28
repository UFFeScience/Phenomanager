package com.uff.phenomanager.domain.core.filter;

import java.util.ArrayList;
import java.util.List;

public class FilterOrder {
    
    private String field;
    private SortOrder sortOrder;
	
    public String getField() {
		return field;
	}
	
	public void setField(String field) {
		this.field = field;
	}
	
	public SortOrder getSortOrder() {
		return sortOrder;
	}

	public void setSortOrder(SortOrder sortOrder) {
		this.sortOrder = sortOrder;
	}
	
	public static List<FilterOrder> buildFilterOrders(String sort) {
		List<FilterOrder> filterOrders = new ArrayList<>();
		
		if (sort == null) {
			return filterOrders;
		}
		
		FilterOrder filterOrder = new FilterOrder();
		StringBuilder sortField = new StringBuilder();
		
		for (int i = 0; i < sort.length(); i++) {
			
			if (sort.charAt(i) != ',') {
				sortField.append(sort.charAt(i));
			} else {
				fillSortValues(filterOrder, sortField.toString());
				filterOrders.add(filterOrder);
				filterOrder = new FilterOrder();
				sortField = new StringBuilder();
			}
		}
		
		fillSortValues(filterOrder, sortField.toString());
		filterOrders.add(filterOrder);
		
		return filterOrders;
	}

	private static void fillSortValues(FilterOrder filterOrder, String sortField) {
		StringBuilder field = new StringBuilder();
		SortOrder sortOrder = SortOrder.ASC;
		
		for (int i = 0; i < sortField.length(); i++) {
			if (sortField.charAt(i) != '=') {
				field.append(sortField.charAt(i));
			} else {
				sortOrder = getSortOrder(sortField, sortOrder, i);
				break;
			}
		}
		
		filterOrder.setField(field.toString().trim());
		filterOrder.setSortOrder(sortOrder);
	}

	private static SortOrder getSortOrder(String sortField, SortOrder sortOrder, int index) {
		StringBuilder sortOrderValue = new StringBuilder();
		
		if ((index + 1) < sortField.length()) {
			sortOrderValue.append(sortField.substring(index + 1, sortField.length()));
		}
		
		SortOrder order = SortOrder.getSortOrder(sortOrderValue.toString().trim());
		if (sortOrder != null) {
			sortOrder = order;
		}
		
		return sortOrder;
	}

	@Override
	public String toString() {
		return "FilterOrder [field=" + field + ", sortOrder=" + sortOrder + "]";
	}
    
}