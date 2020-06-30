package com.uff.phenomanager.util;

import java.io.IOException;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.uff.phenomanager.Constants;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class ReflectionUtils {
	
	private static ObjectMapper mapper = new ObjectMapper();
	
	public static List<Object> getFieldList(String value, Class<?> clazz) throws JsonParseException, JsonMappingException, IOException {
		List<Object> values = new ArrayList<>();
		StringBuilder word = new StringBuilder();
		
		for (int i = 0; i < value.length(); i++) {
			if (value.charAt(i) == ',') {
				values.add(getEntityValueParsed(word.toString().trim(), clazz));
				word = new StringBuilder();
			} else {
				word.append(value.charAt(i));
			}
		}
		
		values.add(getEntityValueParsed(word.toString().trim(), clazz));
		
		return values;
	}
	
	public static void setEntityFieldByClass(Object parentEntity, Object childEntity, Class childEntityClass) throws IllegalArgumentException, IllegalAccessException {
		for (Field field : parentEntity.getClass().getDeclaredFields()) {
			if (field.getType().equals(childEntityClass)) {
				field.setAccessible(true);
				field.set(parentEntity, childEntity);
				break;
			}
		}
	}
	
	public static Field getEntityFieldByName(Class<?> clazz, String fieldName) throws NoSuchFieldException, SecurityException {
		try {
			return clazz.getDeclaredField(fieldName);
		
		} catch (NoSuchFieldException e) {
			Class<?> superClazz = clazz.getSuperclass();
			
			while (superClazz != null) {
				try {
					return superClazz.getDeclaredField(fieldName);
				
				} catch (NoSuchFieldException ex) {
					superClazz = superClazz.getSuperclass();
				}
			}
			
			throw new NoSuchFieldException(e.getMessage());
		}
	}
	
	public static Object getEntityValueParsed(String value, Class<?> clazz) throws JsonParseException, JsonMappingException, IOException {
		if (value != null && !"".equals(value)) {
			if (clazz.equals(Long.class)) {
				return Double.valueOf(value).longValue();
			
			} else if (clazz.equals(Double.class)) {
				return Double.valueOf(value);
			
			} else if (clazz.equals(Float.class)) {
				return Float.valueOf(value);
			
			} else if (clazz.equals(Integer.class)) {
				return Double.valueOf(value).intValue();
			
			} else if (clazz.equals(BigDecimal.class)) {
				return new BigDecimal(value.trim());
				
			} else if (clazz.equals(Date.class)) {
				return Double.valueOf(value);
			
			} else if (clazz.equals(Calendar.class)) {
				if (StringParserUtils.isNumeric(value)) {
					return CalendarUtils.createCalendarFromMiliseconds(Double.valueOf(value).longValue());
				}
				return CalendarUtils.createCalendarFromString(value, Constants.DEFAULT_DATE_FORMAT);
			
			} else if (clazz.equals(String.class)) {
				return value.toString();
			
			} else if (clazz.isEnum()) {
				try {
					return Enum.valueOf((Class<? extends Enum>) clazz, value);
				} catch (IllegalArgumentException | NullPointerException e) {
					return value != null ? value.toUpperCase() : value;
				}
				
			} else {
				return mapper.readValue(value, clazz);
			}
		}
		
		return value;
	}
	
}