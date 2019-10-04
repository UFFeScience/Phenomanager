package com.uff.phenomanager.repository.core;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Selection;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.uff.phenomanager.Constants;
import com.uff.phenomanager.Constants.MSG_ERROR;
import com.uff.phenomanager.domain.core.filter.FilterExpression;
import com.uff.phenomanager.domain.core.filter.FilterField;
import com.uff.phenomanager.domain.core.filter.FilterOrder;
import com.uff.phenomanager.domain.core.filter.LogicOperator;
import com.uff.phenomanager.domain.core.filter.RequestFilter;
import com.uff.phenomanager.exception.BadRequestApiException;
import com.uff.phenomanager.util.ReflectionUtils;
import com.uff.phenomanager.util.StringParserUtils;

@SuppressWarnings({ "unchecked", "rawtypes" } )
public class ApiFilterParser<ENTITY> {
	
	public List<Selection<? extends Object>> getGroupByFields(RequestFilter requestFilter, Root<?> root, Class<ENTITY> entityClass) throws BadRequestApiException {
		try {
			List<String> groupByFields = requestFilter.getParsedGroupBy();
			return buildProjectionSelection(root, entityClass, groupByFields);
			
		} catch (Exception e) {
			throw new BadRequestApiException(String.format(MSG_ERROR.PARSE_PROJECTIONS_ERROR, requestFilter.getProjection()), e);
		}
	}
	
	public List<Selection<? extends Object>> getProjectionFields(RequestFilter requestFilter, Root<?> root, Class<ENTITY> entityClass) throws BadRequestApiException {
		try {
			List<String> projectionFields = requestFilter.getParsedProjection();
			return buildProjectionSelection(root, entityClass, projectionFields);
			
		} catch (Exception e) {
			throw new BadRequestApiException(String.format(MSG_ERROR.PARSE_PROJECTIONS_ERROR, requestFilter.getProjection()), e);
		}
	}
	
	private List<Selection<? extends Object>> buildProjectionSelection(Root<?> root, Class<ENTITY> entityClass,
			List<String> projectionFields) throws NoSuchFieldException {
		
		List<Selection<? extends Object>> projection = new ArrayList<>();
		
		if (!projectionFields.isEmpty()) {
			
			for (String fieldName : projectionFields) {
				List<Field> fields = splitFields(entityClass, fieldName);
				projection.add(buildFieldExpression(fields, root));
			}
		}
		
		return projection;
	}
	
	public List<Selection<? extends Object>> buildAggregateSelection(Root<?> root, CriteriaBuilder criteriaBuilder, Class<ENTITY> entityClass,
			RequestFilter requestFilter) throws BadRequestApiException {
		try {
			List<String> sumFields = requestFilter.getParsedSum();
			List<String> avgFields = requestFilter.getParsedAvg();
			List<String> countFields = requestFilter.getParsedCount();
			List<String> countDistinctFields = requestFilter.getParsedCountDistinct();
			List<String> groupByFields = requestFilter.getParsedGroupBy();
			
			List<Selection<? extends Object>> aggregationFields = new ArrayList<>();
			
			if (!sumFields.isEmpty()) {
				for (String fieldName : sumFields) {
					List<Field> fields = splitFields(entityClass, fieldName);
					aggregationFields.add(criteriaBuilder.sum(buildFieldExpression(fields, root)));
				}
			}
			
			if (!countFields.isEmpty()) {
				for (String fieldName : countFields) {
					List<Field> fields = splitFields(entityClass, fieldName);
					aggregationFields.add(criteriaBuilder.count(buildFieldExpression(fields, root)));
				}
			}
			
			if (!countDistinctFields.isEmpty()) {
				for (String fieldName : countDistinctFields) {
					List<Field> fields = splitFields(entityClass, fieldName);
					aggregationFields.add(criteriaBuilder.countDistinct(buildFieldExpression(fields, root)));
				}
			}
			
			if (!avgFields.isEmpty()) {
				for (String fieldName : avgFields) {
					List<Field> fields = splitFields(entityClass, fieldName);
					aggregationFields.add(criteriaBuilder.avg(buildFieldExpression(fields, root)));
				}
			}
			
			if (!groupByFields.isEmpty()) {
				for (String fieldName : groupByFields) {
					List<Field> fields = splitFields(entityClass, fieldName);
					aggregationFields.add(buildFieldExpression(fields, root));
				}
			}
			
			return aggregationFields;
			
		} catch (Exception e) {
			throw new BadRequestApiException(String.format(MSG_ERROR.PARSE_PROJECTIONS_ERROR, requestFilter.getProjection()), e);
		}
	}

	public List<Predicate> getRestrictions(
			Class<ENTITY> entityClass,
			RequestFilter requestFilter, 
			CriteriaBuilder criteriaBuilder,
			Root<?> root) throws BadRequestApiException {
		try {
			FilterExpression currentExpression = FilterExpression.buildFilterExpressions(requestFilter.getFilter());
			List<Predicate> restrictions = new ArrayList<>();
			
			while (currentExpression != null) {
				List<Predicate> conjunctionRestrictions = new ArrayList<>();
				
				if (currentExpression.getFilterField() != null) {
					if (LogicOperator.OR.equals(currentExpression.getLogicOperator())) {
						
						do {
							conjunctionRestrictions.add(buildPredicate(entityClass, currentExpression.getFilterField(), criteriaBuilder, root));
							currentExpression = currentExpression.getFilterExpression();
						} while (currentExpression != null && LogicOperator.OR.equals(currentExpression.getLogicOperator()));
						
						if (currentExpression != null && currentExpression.getFilterField() != null) {
							conjunctionRestrictions.add(buildPredicate(entityClass, currentExpression.getFilterField(), criteriaBuilder, root));
						}
						
						List<Predicate> orParsedRestrictions = new ArrayList<>();
						orParsedRestrictions.add(criteriaBuilder.or(conjunctionRestrictions.toArray(new Predicate[]{})));
					
						restrictions.add(criteriaBuilder.and(orParsedRestrictions.toArray(new Predicate[]{})));

					} else {
						conjunctionRestrictions.add(buildPredicate(entityClass, currentExpression.getFilterField(), criteriaBuilder, root));
						restrictions.add(criteriaBuilder.and(conjunctionRestrictions.toArray(new Predicate[]{})));
					}
				}
				
				currentExpression = currentExpression.getFilterExpression();
			}
	        
			return restrictions;
		
		} catch (Exception e) {
			throw new BadRequestApiException(String.format(MSG_ERROR.PARSE_FILTER_FIELDS_ERROR, requestFilter.getFilter()), e);
		}
	}

	private Predicate buildPredicate(
			Class<ENTITY> entityClass,
			FilterField filterField, 
			CriteriaBuilder criteriaBuilder, 
			Root<?> root) throws NoSuchFieldException, SecurityException, JsonParseException, JsonMappingException, IOException {
		
		List<Field> fields = splitFields(entityClass, filterField.getField());
		Field field = null;
		
		switch (filterField.getFilterOperator()) {
			case IN:
				field = getSignificantField(entityClass, fields);
				return buildFieldExpression(fields, root)
						.in(ReflectionUtils.getFieldList(
								StringParserUtils.replace(StringParserUtils.replace(filterField.getValue(), "(", ""), ")", ""),
								field.getType()));
			case OU:
				field = getSignificantField(entityClass, fields);
				return buildFieldExpression(fields, root)
						.in(ReflectionUtils.getFieldList(filterField.getValue(), field.getType())).not();
			case GE:
				return criteriaBuilder.greaterThanOrEqualTo(buildFieldExpression(
						fields, root), (Comparable) getTipifiedValue(entityClass, filterField, root, fields));
			case GT:
				return criteriaBuilder.greaterThan(buildFieldExpression(
						fields, root), (Comparable) getTipifiedValue(entityClass, filterField, root, fields));
			case LE:
				return criteriaBuilder.lessThanOrEqualTo(buildFieldExpression(
						fields, root), (Comparable) getTipifiedValue(entityClass, filterField, root, fields));
			case LT:
				return criteriaBuilder.lessThan(buildFieldExpression(
						fields, root), (Comparable) getTipifiedValue(entityClass, filterField, root, fields));
			case NE:
				if (Constants.NULL_VALUE.equals(filterField.getValue())) {
					return criteriaBuilder.isNotNull(buildFieldExpression(fields, root));
				}
				return criteriaBuilder.notEqual(buildFieldExpression(
						fields, root), (Comparable) getTipifiedValue(entityClass, filterField, root, fields));
			case LK:
				return criteriaBuilder.like(criteriaBuilder.upper(buildFieldExpression(fields, root)), 
						"%" + ((String) getTipifiedValue(entityClass, filterField, root, fields)).toUpperCase() + "%");
			case EQ:
			default:
				if (Constants.NULL_VALUE.equals(filterField.getValue())) {
					return criteriaBuilder.isNull(buildFieldExpression(fields, root));
				}
				return criteriaBuilder.equal(buildFieldExpression(
						fields, root), (Comparable) getTipifiedValue(entityClass, filterField, root, fields));
		}
	}

	private List<Field> splitFields(Class<ENTITY> entityClass, String fieldName) throws NoSuchFieldException, SecurityException {
		List<Field> fields = new ArrayList<>();
		List<String> attributeNames =  StringParserUtils.splitStringList(fieldName, '.');
		Class<?> currentFieldClass = entityClass;
		
		for (String attributeName : attributeNames) {
			Field field = ReflectionUtils.getEntityFieldByName(currentFieldClass, attributeName);
			currentFieldClass = field.getType();
			fields.add(field);
		}
		
		return fields;
	}

	private Object getTipifiedValue(Class<ENTITY> entityClass, FilterField filterField, Root<?> root, List<Field> fields)
			throws NoSuchFieldException, JsonParseException, JsonMappingException, IOException {
		Field field = getSignificantField(entityClass, fields);
		return ReflectionUtils.getEntityValueParsed(filterField.getValue(), field.getType());
	}

	private Field getSignificantField(Class<ENTITY> entityClass, List<Field> fields) throws NoSuchFieldException {
		if (fields.isEmpty()) {
			return null;
		}
		
		return fields.get(fields.size() - 1);
	}
	
	private Expression buildFieldExpression(List<Field> fields, Root<?> root) {
		Path<ENTITY> expressionPath = null;
		
		for (Field field : fields) {
			if (expressionPath == null) {
				expressionPath = root.get(field.getName());
				expressionPath.alias(field.getName());
			} else {
				expressionPath = expressionPath.get(field.getName());
				expressionPath.alias(field.getName());
			}
		}

		return expressionPath;
	}
	
	public List<Order> getOrders(
			RequestFilter requestFilter, 
			CriteriaBuilder criteriaBuilder,
			Root<?> root,
			Class<ENTITY> entityClass) throws BadRequestApiException {
		
		try {
			List<FilterOrder> filterOrders = FilterOrder.buildFilterOrders(requestFilter.getSort());
			List<Order> orders = new ArrayList<>();
	        
			if (filterOrders != null && !filterOrders.isEmpty()) {
	        	
	        	for (FilterOrder filterOrder : filterOrders) {
	        		List<Field> fields =  splitFields(entityClass, filterOrder.getField());

	        		switch (filterOrder.getSortOrder()) {
	        			case DESC:
	        				orders.add(criteriaBuilder.desc(buildFieldExpression(fields, root)));
	        				break;
	        			case ASC:
	        			default:
	        				orders.add(criteriaBuilder.asc(buildFieldExpression(fields, root)));
	        				break;
	        		}
	        	}
	        }
			
			return orders;
		
		} catch (Exception e) {
			throw new BadRequestApiException(String.format(MSG_ERROR.PARSE_SORT_ORDER_ERROR, requestFilter.getSort()));
		}
	}
	
}