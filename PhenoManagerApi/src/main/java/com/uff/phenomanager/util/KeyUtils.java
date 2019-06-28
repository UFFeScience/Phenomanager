package com.uff.phenomanager.util;

import java.util.UUID;

public class KeyUtils {
	
	public static String generate() {
		return StringParserUtils.replace(UUID.randomUUID().toString(), "-", "").toUpperCase();
	}

}