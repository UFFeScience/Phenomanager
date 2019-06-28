package com.uff.model.invoker.util;

public class StringParserUtils {
	
	public static String replace(String source, String originalString, String newString) {
	    if (source == null) {
	        return null;
	    }
	    
	    int i = 0;
	    if ((i = source.indexOf(originalString, i)) >= 0) {
	        char[] sourceArray = source.toCharArray();
	        char[] nsArray = newString.toCharArray();
	        int oLength = originalString.length();

	        StringBuilder buf = new StringBuilder (sourceArray.length);
	        buf.append (sourceArray, 0, i).append(nsArray);
	        i += oLength;
	        int j = i;
	        
	        while ((i = source.indexOf(originalString, i)) > 0) {
	            buf.append (sourceArray, j, i - j).append(nsArray);
	            i += oLength;
	            j = i;
	        }
	    
	        buf.append (sourceArray, j, sourceArray.length - j);
	        source = buf.toString();
	        buf.setLength (0);
	    }
	    
	    return source;
	}
	
}