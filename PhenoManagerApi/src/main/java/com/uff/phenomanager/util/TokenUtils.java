package com.uff.phenomanager.util;

import java.util.List;

import com.uff.phenomanager.Constants.JWT_AUTH;

public class TokenUtils {
	
	public static String getTokenFromAuthorizationHeader(String authorizationHeader) {
		if (authorizationHeader != null && !"".equals(authorizationHeader)) {
			List<String> authorizationHeaderData = StringParserUtils.splitStringList(authorizationHeader, ' ');
			
			if (authorizationHeaderData != null && authorizationHeaderData.size() > 1 && 
					authorizationHeaderData.get(0).equals(JWT_AUTH.BEARER)) {
				
				return authorizationHeaderData.get(1);
			}
		}	
		
		return null;
   }

}