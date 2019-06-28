package com.uff.phenomanager.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.uff.phenomanager.Constants;
import com.uff.phenomanager.Constants.UPLOAD;
import com.uff.phenomanager.domain.User;
import com.uff.phenomanager.domain.api.google.DriveFile;
import com.uff.phenomanager.exception.ApiException;
import com.uff.phenomanager.exception.NotFoundApiException;
import com.uff.phenomanager.repository.UserRepository;
import com.uff.phenomanager.service.api.SciManagerService;
import com.uff.phenomanager.service.api.google.GoogleDriveService;
import com.uff.phenomanager.service.core.ApiRestService;
import com.uff.phenomanager.util.EncrypterUtils;
import com.uff.phenomanager.util.FileUtils;
import com.uff.phenomanager.util.StringParserUtils;

@Service
public class UserService extends ApiRestService<User, UserRepository> {
	
	private static final Logger log = LoggerFactory.getLogger(UserService.class);
	
	@Autowired
	private UserRepository userRepository;
	
	@Lazy
	@Autowired
	private GoogleDriveService googleDriveService;
	
	@Lazy
	@Autowired
	private SciManagerService sciManagerService;
	
	@Override
	protected UserRepository getRepository() {
		return userRepository;
	}
	
	@Override
	protected Class<User> getEntityClass() {
		return User.class;
	}
	
	@Override
	public User save(User user) throws ApiException {
		user.setPassword(EncrypterUtils.encryptPassword(user.getPassword()));
		return super.save(user);
	}
	
	@Override
	public User update(User user) throws ApiException {
		if (user.getImageContentText() != null && !"".equals(user.getImageContentText())) {
			try {
				
				if (user.getProfileImageFileId() != null && !"".equals(user.getProfileImageFileId())) {
					try {
						googleDriveService.deleteFileAsync(user.getProfileImageFileId());
						
					} catch (NotFoundApiException e) {
						log.warn(Constants.MSG_WARN.GOOGLE_FILE_NOT_FOUND, user.getProfileImageFileId());
					}
				}
				
				byte[] imageBytes = FileUtils.processImageData(user.getImageContentText());
				String tmpFilePath = FileUtils.buildTmpPath(user.getSlug());
				
		    	FileOutputStream fos = new FileOutputStream(tmpFilePath);
		    	fos.write(imageBytes);
		    	fos.close();
		    	
		    	String parentFolderHash = new StringBuilder(
						StringParserUtils.replace(user.getName(), " ", "_"))
						.append("_")
						.append(user.getSlug()).toString();
		    	
				DriveFile driveFile = googleDriveService.uploadFile(parentFolderHash, new File(tmpFilePath), 
						UPLOAD.PROFILE_IMAGES_FOLDER, Boolean.TRUE);
				
				if (driveFile != null) {
					user.setProfileImageFileId(driveFile.getFileId());
				}
				
			} catch (IOException e) {
				log.error(Constants.MSG_ERROR.GET_DRIVE_UPLOAD_FILE_ERROR, e);
			}
		}
		
		return super.update(user);
	}
	
	@Override
	public Integer delete(String slug) throws NotFoundApiException {
		User user = findBySlug(slug);
		
		if (user.getProfileImageFileId() != null && !"".equals(user.getProfileImageFileId())) {
			try {
				googleDriveService.deleteFileAsync(user.getProfileImageFileId());
				
			} catch (NotFoundApiException e) {
				log.warn(Constants.MSG_WARN.GOOGLE_FILE_NOT_FOUND, user.getProfileImageFileId());
			}
		}
		
		return super.delete(slug);
	}
	
	public User getUserByEmailAndActive(String email, Boolean active) {
		return userRepository.findByEmailAndActive(email, active);
	}

	public void sync(String slug, String authorization) throws NotFoundApiException {
		User user = findBySlug(slug);
		sciManagerService.syncUser(user, authorization);
	}
	
}