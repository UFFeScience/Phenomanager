package com.uff.phenomanager.repository.core.mapper;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

import javax.persistence.criteria.Path;
import javax.persistence.criteria.Selection;

import org.hibernate.jpa.criteria.expression.function.AggregationFunction;
import org.hibernate.mapping.Set;
import org.springframework.stereotype.Component;

import com.uff.phenomanager.domain.core.BaseApiEntity;
import com.uff.phenomanager.domain.core.filter.AggregateFunction;
import com.uff.phenomanager.util.ReflectionUtils;

@Component
@SuppressWarnings({ "unchecked", "rawtypes" })
public class ApiResultMapper<ENTITY extends BaseApiEntity> {
	
	public List<ENTITY> mapResultSet(
			Class<ENTITY> entityClass, 
			List<Object> result, 
			List<Selection<? extends Object>> projection) throws Exception {
		
		List<ENTITY> entities = new ArrayList<>();
		
		for (Object row : result) {
			
			if (row != null) {
				if (Object[].class.equals(row.getClass())) {
					mapSimpleValuesProjection(entityClass, projection, entities, row);
					
				} else if (entityClass.equals(row.getClass())) {
					mapMultivaluedValuesProjection(entityClass, projection, entities, row);
					
				} else {
					mapSingleMultivaluedValueData(entityClass, projection, entities, row);
				}
			}
		}
		
		return entities;
	}
	
	private void mapSimpleValuesProjection(Class<ENTITY> entityClass, List<Selection<? extends Object>> projection,
			List<ENTITY> entities, Object row) throws Exception {
		
		Object[] fieldData = (Object[]) row;
		
		Constructor<?> constructor = entityClass.getConstructor();
		ENTITY object = (ENTITY) constructor.newInstance();
		
		for (int i = 0; i < fieldData.length; i++) {
		
			if (projection.get(i) instanceof AggregationFunction) {
				AggregationFunction aggregationFunction = (AggregationFunction) projection.get(i);
				Path<Object> attributePath = (Path<Object>) aggregationFunction.getArgumentExpressions().get(0);
				
				if (AggregateFunction.isCountFunction(aggregationFunction.getFunctionName())) {
					object.getCount().put(attributePath.getAlias(), (Long) fieldData[i]);
					
				} else if (AggregateFunction.isCountDistinctFunction(aggregationFunction.getFunctionName())) {
					object.getCountDistinct().put(attributePath.getAlias(), (Long) fieldData[i]);
					
				} else if (AggregateFunction.isSumFunction(aggregationFunction.getFunctionName())) {
					if (fieldData[i].getClass().equals(Double.class)) {
						object.getSum().put(attributePath.getAlias(), BigDecimal.valueOf((Double) fieldData[i]));
					} else {
						object.getSum().put(attributePath.getAlias(), BigDecimal.valueOf((Long) fieldData[i]));
					}
					
				} else if (AggregateFunction.isAvgFunction(aggregationFunction.getFunctionName())) {
					if (fieldData[i].getClass().equals(Double.class)) {
						object.getAvg().put(attributePath.getAlias(), BigDecimal.valueOf((Double) fieldData[i]));
					} else {
						object.getAvg().put(attributePath.getAlias(), BigDecimal.valueOf((Long) fieldData[i]));
					}
				}
				
			} else {
				Path<Object> attributePath = (Path<Object>) projection.get(i);
				setProjectionAggregationField(entityClass, fieldData[i], object, attributePath);
			}
		}
		
		entities.add(object);
	}

	private void mapMultivaluedValuesProjection(Class<ENTITY> entityClass, List<Selection<? extends Object>> projection,
			List<ENTITY> entities, Object row)
			throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
		
		ENTITY entity = (ENTITY) row;
		
		if (projection == null || projection.isEmpty()) {
			entities.add(entity);
			return;
		}
		
		Constructor<?> constructor = entityClass.getConstructor();
		ENTITY object = (ENTITY) constructor.newInstance();
		
		for (Field field : entityClass.getDeclaredFields()) {
		    field.setAccessible(true);
		    
			if (isInProjection(field.getName(), projection)) {
				field.set(object, field.get(entity));
			}
		}
		
		entities.add(object);
	}
	
	private Boolean isInProjection(String fieldName, List<Selection<? extends Object>> projection) {
		if (projection == null || projection.isEmpty()) {
			return Boolean.FALSE;
		}
		
		for (Selection<? extends Object> projectionField : projection) {
			if (fieldName.equals(projectionField.getAlias())) {
				return Boolean.TRUE;
			}
		}
		
		return Boolean.FALSE;
	}

	private void mapSingleMultivaluedValueData(Class<ENTITY> entityClass, List<Selection<? extends Object>> projection,
			List<ENTITY> entities, Object row) throws Exception {
		
		Constructor<?> constructor = entityClass.getConstructor();
		ENTITY object = (ENTITY) constructor.newInstance();
		
		if (projection.get(0) instanceof AggregationFunction) {
			AggregationFunction aggregationFunction = (AggregationFunction) projection.get(0);
			Path<Object> attributePath = (Path<Object>) aggregationFunction.getArgumentExpressions().get(0);
			
			if (AggregateFunction.isCountFunction(aggregationFunction.getFunctionName()) ||
					AggregateFunction.isCountDistinctFunction(aggregationFunction.getFunctionName())) {
				object.getCount().put(attributePath.getAlias(), (Long) row);
				
			} else if (AggregateFunction.isCountDistinctFunction(aggregationFunction.getFunctionName())) {
				object.getCountDistinct().put(attributePath.getAlias(), (Long) row);
				
			} else if (AggregateFunction.isSumFunction(aggregationFunction.getFunctionName())) {
				object.getSum().put(attributePath.getAlias(), (BigDecimal) row);
				
			} else if (AggregateFunction.isAvgFunction(aggregationFunction.getFunctionName())) {
				object.getAvg().put(attributePath.getAlias(), new BigDecimal((Double) row));
			}
			
		} else {
			Path<Object> attributePath = (Path<Object>) projection.get(0);
			setProjectionAggregationField(entityClass, row, object, attributePath);
		}
		
		entities.add(object);
	}
	
	private void setProjectionAggregationField(Class<ENTITY> entityClass, Object fieldData, ENTITY object,
			Path<Object> attributePath) throws Exception {
		
		if (attributePath.getParentPath() != null && !attributePath.getParentPath().getJavaType().equals(entityClass)) {
			Constructor<?> constructorField = attributePath.getParentPath().getJavaType().getConstructor();
			Object fieldObject = constructorField.newInstance();
			
			Field childField = ReflectionUtils.getEntityFieldByName(attributePath.getParentPath().getJavaType(), attributePath.getAlias());
			childField.setAccessible(true);
			setFieldValueResult(fieldData, fieldObject, childField);
			
			Field field = ReflectionUtils.getEntityFieldByName(entityClass, attributePath.getParentPath().getAlias());
			field.setAccessible(true);
			setFieldValueResult(fieldObject, object, field);

		} else {
			Field field = ReflectionUtils.getEntityFieldByName(entityClass, attributePath.getAlias());
			field.setAccessible(true);
			setFieldValueResult(fieldData, object, field);
		}
	}

	private void setFieldValueResult(Object fieldDataValue, Object object, Field field) throws Exception {
		if (Collection.class.isAssignableFrom(field.getType())) {
			Collection collection = (Collection) field.get(object);
			
			if (collection == null) {
				
				if (Set.class.equals(field.getType())) {
					collection = new HashSet<>();
				
				} else if (Queue.class.equals(field.getType())) {
					collection = new PriorityQueue<>();
				
				} else {
					collection = new ArrayList<>();
				}
			}
			
			collection.add(fieldDataValue);
			field.set(object, collection);
			
		} else {
			field.set(object, fieldDataValue);
		}
	}

}