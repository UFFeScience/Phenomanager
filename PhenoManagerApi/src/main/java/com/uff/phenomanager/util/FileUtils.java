package com.uff.phenomanager.util;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

import javax.imageio.ImageIO;

import org.apache.commons.codec.binary.Base64;
import org.apache.tika.Tika;
import org.springframework.web.multipart.MultipartFile;

import com.uff.phenomanager.Constants;
import com.uff.phenomanager.Constants.CONTROLLER;

public class FileUtils {
	
	public static String identifyFileType(File file) throws IOException {
		return new Tika().detect(file);
	}
	
	public static File multipartToFile(MultipartFile multipart, String fileName) throws IllegalStateException, IOException {
	    File convertedFile = new File(buildTmpPath(fileName));
	    multipart.transferTo(convertedFile);
	    
	    return convertedFile;
	}
	
	public static String buildTmpPath(String fileName) {
		return  new StringBuilder(System.getProperty(Constants.TMP_DIR))
				.append(CONTROLLER.PATH_SEPARATOR)
				.append(fileName)
				.append("-")
				.append(new Date().getTime()).toString();
	}

	public static byte[] processImageData(String imageDataText) throws IOException {
		File tempImageFile = File.createTempFile(Constants.TEMP_FILE_PREFIX, Constants.TEMP_FILE_SUFFIX, null);
		byte dearr[] = Base64.decodeBase64(imageDataText.substring(Constants.IMAGE_BASE_64_PREFIX.length()));
	    
		FileOutputStream fos = new FileOutputStream(tempImageFile); 
	    fos.write(dearr); 
	    fos.close();
	    
	    return resizeImage(tempImageFile);
	}

	public static byte[] resizeImage(File imageFile) throws IOException {
		BufferedImage originalImage = ImageIO.read(imageFile);
		
		if (originalImage.getWidth() < Constants.IMG_WIDTH && originalImage.getHeight() < Constants.IMG_HEIGHT) {
			byte[] bytesArray = new byte[(int) imageFile.length()];

			FileInputStream fileInputStream = new FileInputStream(imageFile);
	        fileInputStream.read(bytesArray);
	        fileInputStream.close();
			
			return bytesArray;
		}
		
		int type = originalImage.getType() == 0 ? BufferedImage.TYPE_INT_ARGB : originalImage.getType();
				
		BufferedImage resizedImageHintPng = resizeImageWithHint(originalImage, type);
		ImageIO.write(resizedImageHintPng, Constants.IMAGE_FORMAT, imageFile);
		
		byte[] bytesArray = new byte[(int) imageFile.length()];

		FileInputStream fileInputStream = new FileInputStream(imageFile);
        fileInputStream.read(bytesArray);
        fileInputStream.close();
		
		return bytesArray;
	}
	
	private static BufferedImage resizeImageWithHint(BufferedImage originalImage, int type) {
		BufferedImage resizedImage = new BufferedImage(Constants.IMG_WIDTH, Constants.IMG_HEIGHT, type);
		Graphics2D graphics = resizedImage.createGraphics();
		
		graphics.setComposite(AlphaComposite.Src);

		graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
		graphics.drawImage(originalImage.getScaledInstance(Constants.IMG_WIDTH, Constants.IMG_WIDTH, Image.SCALE_SMOOTH), 
				0, 0, Constants.IMG_WIDTH, Constants.IMG_HEIGHT, null);
		graphics.dispose();

		return resizedImage;
    }
	
}