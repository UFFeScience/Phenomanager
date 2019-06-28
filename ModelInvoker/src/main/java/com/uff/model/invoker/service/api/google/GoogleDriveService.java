package com.uff.model.invoker.service.api.google;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.uff.model.invoker.Constants.UPLOAD;
import com.uff.model.invoker.domain.api.google.DriveFile;
import com.uff.model.invoker.exception.GoogleErrorApiException;
import com.uff.model.invoker.exception.NotFoundApiException;
import com.uff.model.invoker.repository.google.GoogleDriveRepository;

@Lazy
@Service
public class GoogleDriveService {
	
	@Autowired
	private GoogleDriveRepository googleDriveRepository; 
	
	public DriveFile uploadFile(
			String folderHashName, 
			File file, 
			String childFolder, 
			Boolean publicFile) throws GoogleErrorApiException {
		
		List<String> folderStructure = buildFolderStructure(folderHashName, childFolder);
		return googleDriveRepository.uploadFileToFolder(folderStructure, file, publicFile);
	}
	
	public List<DriveFile> getFolderFiles(String folderHashName, String childFolder) {
		List<String> folderStructure = buildFolderStructure(folderHashName, childFolder);
		return googleDriveRepository.getFolderFiles(folderStructure);
	}

	private List<String> buildFolderStructure(String folderHashName, String childFolder) {
		List<String> folderStructure = new ArrayList<>();
		
		String parentFolder = new StringBuilder(folderHashName).toString();
		
		folderStructure.add(UPLOAD.ROOT_FOLDER);
		folderStructure.add(parentFolder);
		folderStructure.add(childFolder);
		
		return folderStructure;
	}
	
	@Async
	private Boolean deleteFileAsync(String fileId) throws NotFoundApiException {
		return deleteFile(fileId);
	}
	
	public Boolean deleteFile(String fileId) throws NotFoundApiException {
		return googleDriveRepository.deleteFile(fileId);
	}
	
	public byte[] getFileBytesContent(String fileId) throws NotFoundApiException {
		return googleDriveRepository.downloadFile(fileId);
	}
	
}