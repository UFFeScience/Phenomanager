package com.uff.model.invoker.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

import org.apache.tika.Tika;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.uff.model.invoker.Constants;

public class FileUtils {
	
	private static final Logger log = LoggerFactory.getLogger(FileUtils.class);
	
	public static String identifyFileType(File file) throws IOException {
		return new Tika().detect(file);
	}
	
	public static String buildTmpPath(String fileName) {
		return new StringBuilder(System.getProperty(Constants.TMP_DIR))
				.append(Constants.PATH_SEPARATOR)
				.append(fileName)
				.append("-")
				.append(new Date().getTime()).toString();
	}
	
	public static File writeStringToFile(String prefix, String sufix, String textContent) throws IOException {
		BufferedWriter writer = null;
		File tempFile = null;
		
		try {
			tempFile = File.createTempFile(prefix, sufix);
		    writer = new BufferedWriter(new FileWriter(tempFile));
		    writer.write(textContent);
		
		} catch (IOException e) {
			log.error("Error while writing String [{}] to file prefix [{}], textContent", prefix);
		
		} finally {
		    
			try {
		        if (writer != null) {
		        	writer.close( );
		        }
		    
		    } catch (IOException e) {
		    	log.error("Error closing file of content String [{}] in file prefix [{}]", textContent, prefix);
		    }
		}
		
		return tempFile;
	}
	
}