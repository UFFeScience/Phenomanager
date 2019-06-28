package com.uff.phenomanager.repository.core;

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

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Selection;

import org.hibernate.jpa.criteria.expression.function.AggregationFunction;
import org.hibernate.mapping.Set;
import org.postgresql.util.PSQLException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.uff.phenomanager.Constants.MSG_ERROR;
import com.uff.phenomanager.domain.BaseApiEntity;
import com.uff.phenomanager.domain.core.filter.AggregateFunction;
import com.uff.phenomanager.domain.core.filter.RequestFilter;
import com.uff.phenomanager.exception.BadRequestApiException;
import com.uff.phenomanager.exception.InternalErrorApiException;
import com.uff.phenomanager.exception.NotFoundApiException;
import com.uff.phenomanager.util.ReflectionUtils;

@Repository
@SuppressWarnings({ "unchecked", "rawtypes" })
public class ApiFilterRepository<ENTITY extends BaseApiEntity> extends ApiFilterParser<ENTITY> {
	
	@Autowired
	private EntityManager entityManager;
	
	public Long countAll(Class<ENTITY> entityClass, RequestFilter requestFilter) 
			throws NotFoundApiException, BadRequestApiException, InternalErrorApiException {
		
		requestFilter.processSymbols();
		
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<Long> query = criteriaBuilder.createQuery(Long.class);
		Root<?> root = query.from(entityClass);

		query.select(criteriaBuilder.count(root));
		
		List<Predicate> restrictions = getRestrictions(entityClass, requestFilter, criteriaBuilder, root); 
		if (!restrictions.isEmpty()) {
			query.where(restrictions.toArray(new Predicate[]{}));
		}
		
		try {
		    return entityManager.createQuery(query).getSingleResult();
		
		} catch (NoResultException e) {
			throw new NotFoundApiException(String.format(MSG_ERROR.ENTITIES_NOT_FOUND_ERROR, requestFilter), e);
		
		} catch (PersistenceException e) {
			throw new BadRequestApiException(String.format(MSG_ERROR.BAD_REQUEST_ERROR, requestFilter), e);
		
		} catch (Exception e) {
			throw new InternalErrorApiException(String.format(MSG_ERROR.UNEXPECTED_FETCHING_ERROR, requestFilter), e);
		}
	}
	
	public List<ENTITY> findAll(Class<ENTITY> entityClass, RequestFilter requestFilter) 
			throws NotFoundApiException, BadRequestApiException, InternalErrorApiException {
		
		requestFilter.processSymbols();

		if (requestFilter.hasValidAggregateFunction()) {
			return aggregate(entityClass, requestFilter);
		}
		
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<Object> query = criteriaBuilder.createQuery(Object.class);
		Root<?> root = query.from(entityClass);

		List<Selection<? extends Object>> projection =  getProjectionFields(requestFilter, root, entityClass);
		if (!projection.isEmpty() && (projection.size() == 1 || !containsMultiValuedProjection(projection))) {
			query.multiselect(projection.toArray(new Selection[]{}));
		} else {
			query.select(root);
		}
		
		List<Predicate> restrictions = getRestrictions(entityClass, requestFilter, criteriaBuilder, root); 
		if (!restrictions.isEmpty()) {
			query.where(restrictions.toArray(new Predicate[]{}));
		}
		
		List<Order> orders = getOrders(requestFilter, criteriaBuilder, root, entityClass);
		if (!orders.isEmpty()) {
			query.orderBy(orders);
		}
		
		try {
		    List<Object> result = (List<Object>) entityManager.createQuery(query)
		    		.setMaxResults(requestFilter.getFetchLimit())
		    		.setFirstResult(requestFilter.getFetchOffset())
		    		.getResultList();
		    
		    return mapResultSet(entityClass, result, projection);
		    
		} catch (NoResultException e) {
			throw new NotFoundApiException(String.format(MSG_ERROR.ENTITIES_NOT_FOUND_ERROR, requestFilter), e);
		
		} catch (PSQLException | PersistenceException | NumberFormatException e) {
			throw new BadRequestApiException(String.format(MSG_ERROR.BAD_REQUEST_ERROR, requestFilter), e);
			
		} catch (Exception e) {
			throw new InternalErrorApiException(String.format(MSG_ERROR.UNEXPECTED_FETCHING_ERROR, requestFilter), e);
		}
	}
	
	public List<ENTITY> aggregate(Class<ENTITY> entityClass, RequestFilter requestFilter) 
			throws NotFoundApiException, BadRequestApiException, InternalErrorApiException {
		
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<Object> query = criteriaBuilder.createQuery(Object.class);
		Root<?> root = query.from(entityClass);

		List<Selection<? extends Object>> aggregationFields = buildAggregateSelection(root, criteriaBuilder, entityClass, requestFilter);
		
		if (aggregationFields.isEmpty()) {
			throw new BadRequestApiException(String.format(MSG_ERROR.INVALID_AGGREGATION_ERROR, requestFilter));
		}
		
		query.multiselect(aggregationFields.toArray(new Selection[]{}));
		
		List<Predicate> restrictions = getRestrictions(entityClass, requestFilter, criteriaBuilder, root); 
		if (!restrictions.isEmpty()) {
			query.where(restrictions.toArray(new Predicate[]{}));
		}
		
		List<Order> orders = getOrders(requestFilter, criteriaBuilder, root, entityClass);
		if (!orders.isEmpty()) {
			query.orderBy(orders);
		}
		
		List<Selection<? extends Object>> groupBy =  getGroupByFields(requestFilter, root, entityClass);
		query.groupBy(groupBy.toArray(new Expression[]{}));
		
		try {
		    List<Object> result = (List<Object>) entityManager.createQuery(query)
		    		.getResultList();
		    
		    return mapResultSet(entityClass, result, aggregationFields);
		    
		} catch (NoResultException e) {
			throw new NotFoundApiException(String.format(MSG_ERROR.ENTITIES_NOT_FOUND_ERROR, requestFilter), e);
		
		} catch (PSQLException | PersistenceException e) {
			throw new BadRequestApiException(String.format(MSG_ERROR.BAD_REQUEST_ERROR, requestFilter), e);
			
		} catch (Exception e) {
			throw new InternalErrorApiException(String.format(MSG_ERROR.UNEXPECTED_FETCHING_ERROR, requestFilter), e);
		}
	}
	
	protected Boolean containsMultiValuedProjection(List<Selection<? extends Object>> projection) {
		if (projection == null || projection.isEmpty()) {
			return Boolean.FALSE;
		}
		
		for (Selection<? extends Object> projectionField : projection) {
			Path<Object> attributePath = (Path<Object>) projectionField;
			
			if (Collection.class.isAssignableFrom(attributePath.getJavaType())) {
				return Boolean.TRUE;
			}
		}
		
		return Boolean.FALSE;
	}

	protected List<ENTITY> mapResultSet(
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
			List<ENTITY> entities, Object row) throws NoSuchMethodException, InstantiationException,
			IllegalAccessException, InvocationTargetException, NoSuchFieldException, Exception {
		
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
					object.getSum().put(attributePath.getAlias(), (BigDecimal) fieldData[i]);
					
				} else if (AggregateFunction.isAvgFunction(aggregationFunction.getFunctionName())) {
					object.getAvg().put(attributePath.getAlias(), new BigDecimal((Double) fieldData[i]));
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
			List<ENTITY> entities, Object row) throws NoSuchMethodException, InstantiationException,
			IllegalAccessException, InvocationTargetException, NoSuchFieldException, Exception {
		
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
			Path<Object> attributePath) throws NoSuchMethodException, InstantiationException, IllegalAccessException,
			InvocationTargetException, NoSuchFieldException, Exception {
		
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