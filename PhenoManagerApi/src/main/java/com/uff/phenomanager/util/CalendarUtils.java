package com.uff.phenomanager.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.uff.phenomanager.Constants;
import com.uff.phenomanager.Constants.MSG_ERROR;

public class CalendarUtils {
	
	private static final Logger log = LoggerFactory.getLogger(CalendarUtils.class);
	
	public static Calendar createCalendarFromString(String dateText, String dateFormat) {
		if (dateText == null || "".equals(dateText)) {
			return null;
		}
		
		Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone(Constants.DATE_TIMEZONE));
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormat, 
				new Locale(Constants.LOCALE_PT, Constants.LOCALE_BR));

		try {
			calendar.setTime(simpleDateFormat.parse(dateText));
		} 
		catch (ParseException e) {
			log.error(MSG_ERROR.ERROR_PARSE_DATE, dateText, e);
			return null;
		}
		
		return calendar;
	}
	
	public static Calendar createCalendarFromMiliseconds(Long miliseconds) {
		Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone(Constants.DATE_TIMEZONE), 
				new Locale(Constants.LOCALE_PT, Constants.LOCALE_BR));
		calendar.setTimeInMillis(miliseconds);
		return calendar;
	}
	
}