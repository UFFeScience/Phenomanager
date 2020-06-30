package com.uff.phenomanager.util;

import java.util.ArrayList;
import java.util.List;

public class StringParserUtils {
	
	public static List<String> splitStringList(String value, char delimiter) {
		List<String> values = new ArrayList<>();
		
		if (value == null ) {
			return values;
		}

		StringBuilder word = new StringBuilder();
		
		for (int i = 0; i < value.length(); i++) {
			if (value.charAt(i) == delimiter) {
				values.add(word.toString().trim());
				word = new StringBuilder();
			} else {
				word.append(value.charAt(i));
			}
		}
		values.add(word.toString().trim());
		
		return values;
	}
	
	public static String replace(String source, String originalString, String newString) {
	    if (source == null) {
	        return null;
	    }
	    
	    int i = 0;
	    if ((i = source.indexOf(originalString, i)) >= 0) {
	        char[] sourceArray = source.toCharArray();
	        char[] nsArray = newString.toCharArray();
	        int oLength = originalString.length();

	        StringBuilder buf = new StringBuilder(sourceArray.length);
	        buf.append(sourceArray, 0, i).append(nsArray);
	        i += oLength;
	        int j = i;
	        
	        while ((i = source.indexOf(originalString, i)) > 0) {
	            buf.append(sourceArray, j, i - j).append(nsArray);
	            i += oLength;
	            j = i;
	        }
	    
	        buf.append(sourceArray, j, sourceArray.length - j);
	        source = buf.toString();
	        buf.setLength (0);
	    }
	    
	    return source;
	}
	
	public static Boolean isNumeric(String value) {
		for (char ch : value.toCharArray()) {
			if (!Character.isDigit(ch)) {
				return Boolean.FALSE;
			}
		}
			
		return Boolean.TRUE;
	}
	
}