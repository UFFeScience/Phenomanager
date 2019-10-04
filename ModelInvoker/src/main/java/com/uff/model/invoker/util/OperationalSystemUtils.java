package com.uff.model.invoker.util;

public class OperationalSystemUtils {
	
	public enum OS {
        WINDOWS, LINUX, OSX, SOLARIS, BSD
    };

	public static OS getOS(String osBashType) {
		if (osBashType != null) {
			if (osBashType.contains("solaris")) {
				return OS.SOLARIS;
				
			} else if (osBashType.contains("darwin")) {
				return OS.OSX;
				
			} else if (osBashType.contains("linux")) {
				return OS.LINUX;
				
			} else if (osBashType.contains("darwin")) {
				return OS.OSX;
				
			} else if (osBashType.contains("bsd")) {
				return OS.BSD;
				
			} else if (osBashType.contains("msys")) {
				return OS.WINDOWS;
			}
		}
		
	    return null;
	}
	
}