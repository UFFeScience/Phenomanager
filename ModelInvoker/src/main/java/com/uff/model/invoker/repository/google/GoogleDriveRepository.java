package com.uff.model.invoker.repository.google;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.FileContent;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.model.Permission;
import com.uff.model.invoker.Constants.GOOGLE_API;
import com.uff.model.invoker.Constants.MSG_ERROR;
import com.uff.model.invoker.domain.api.google.DriveFile;
import com.uff.model.invoker.exception.GoogleErrorApiException;
import com.uff.model.invoker.exception.NotFoundApiException;
import com.uff.model.invoker.util.FileUtils;

@Repository
public class GoogleDriveRepository extends GoogleRepository {
	
	private static final Logger log = LoggerFactory.getLogger(GoogleDriveRepository.class);
	
	private Drive driveService;
	
	@Override
	public void initializeService() throws GeneralSecurityException, IOException {
		if (driveService == null) {
			driveService = new Drive.Builder(GoogleNetHttpTransport.newTrustedTransport(), 
					JacksonFactory.getDefaultInstance(), setHttpTimeout(credential))
					.setApplicationName(GOOGLE_API.APPLICATION_NAME)
					.build();
		}
	}
	
	public Boolean deleteFile(String fileId) throws NotFoundApiException {
		handleRefreshToken();
		
		try {
			driveService.files().delete(fileId).execute();
			return Boolean.TRUE;

		} catch (IOException e) {
			log.error(MSG_ERROR.GET_DRIVE_DOWNLOAD_FILE_NOT_FOUND, e);
			throw new NotFoundApiException(MSG_ERROR.GET_DRIVE_DOWNLOAD_FILE_NOT_FOUND);
			
		} catch (Exception e) {
			log.error(MSG_ERROR.GET_DRIVE_DOWNLOAD_FILE_ERROR, e);
		}
		
		return Boolean.FALSE;
	}
	
	public DriveFile getFileByNameAndParentFolder(List<String> folderNames, String fileName) throws IOException, NotFoundApiException {
		handleRefreshToken();
		String folderId = getFolderIdByName(folderNames);
		
		FileList result = driveService.files().list()
			.setQ(String.format(GOOGLE_API.FILE_QUERY_NAME_PARENT, fileName, folderId))
			.setSpaces(GOOGLE_API.DRIVE_SPACES)
			.setFields(GOOGLE_API.FOLDER_QUERY_FIELDS)
			.setPageToken(null)
			.execute();
		
		if (result == null || result.getFiles().isEmpty()) {
			log.error(MSG_ERROR.GET_DRIVE_DOWNLOAD_FILE_NOT_FOUND);
			throw new NotFoundApiException(MSG_ERROR.GET_DRIVE_DOWNLOAD_FILE_NOT_FOUND);
		}
		
		File file = result.getFiles().get(0);
		Calendar uploadDate = Calendar.getInstance();
		uploadDate.setTimeInMillis(file.getCreatedTime().getValue());
		
		return DriveFile.builder()
				.fileId(file.getId())
				.fileName(file.getName())
				.uploadDate(uploadDate)
				.build();
	}
	
	public DriveFile downloadFile(String fileId) throws NotFoundApiException {
		handleRefreshToken();
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		
		try {
			outputStream = new ByteArrayOutputStream();
			
			File file = driveService.files().get(fileId).execute();
			
			driveService.files().get(fileId)
			.executeMediaAndDownloadTo(outputStream);
			
			return DriveFile.builder()
					.fileId(file.getId())
					.fileName(file.getName())
					.fileContent(outputStream.toByteArray())
					.build();

		} catch (IOException e) {
			log.error(MSG_ERROR.GET_DRIVE_DOWNLOAD_FILE_NOT_FOUND, e);
			throw new NotFoundApiException(MSG_ERROR.GET_DRIVE_DOWNLOAD_FILE_NOT_FOUND);
			
		} catch (Exception e) {
			log.error(MSG_ERROR.GET_DRIVE_DOWNLOAD_FILE_ERROR, e);
		
		} finally {
			try {
				outputStream.close();
				
			} catch (IOException e) {
				log.error(MSG_ERROR.GET_DRIVE_DOWNLOAD_FILE_ERROR, e);
			}
		}
		
		return null;
	}
	
	public DriveFile uploadFileToFolder(List<String> folderNames, java.io.File file, Boolean publicFile) throws GoogleErrorApiException {
		handleRefreshToken();
		
		try {
			String folderId = getFolderIdByName(folderNames);
			
			File fileMetadata = new File();
			fileMetadata.setName(file.getName());
			fileMetadata.setParents(Collections.singletonList(folderId));

			FileContent mediaContent = new FileContent(FileUtils.identifyFileType(file), file);
			
			File uploadedFile = driveService.files().create(fileMetadata, mediaContent)
					.setFields(GOOGLE_API.FILE_FIELDS)
					.execute();
			
			if (uploadedFile != null) {
				
				if (publicFile) {
					Permission userPermission = new Permission()
						    .setType(GOOGLE_API.ANYONE_PERMISSION_TYPE)
						    .setRole(GOOGLE_API.READER_PERMISSION_TOLE);
					
					driveService.permissions().create(uploadedFile.getId(), userPermission)
					    .setFields(GOOGLE_API.ID_FIELD_BASE).execute();
				}
				
				Calendar uploadDate = Calendar.getInstance();
				uploadDate.setTimeInMillis(uploadedFile.getCreatedTime().getValue());
				
				return DriveFile.builder()
						.fileId(uploadedFile.getId())
						.fileName(uploadedFile.getName())
						.uploadDate(uploadDate)
						.build();
			}

		} catch (Exception e) {
			log.error(MSG_ERROR.GET_DRIVE_UPLOAD_FILE_ERROR, e);
			throw new GoogleErrorApiException(MSG_ERROR.GET_DRIVE_UPLOAD_FILE_ERROR);
		}
		
		return null;
	}
	
	public List<DriveFile> getFolderFiles(List<String> folderNames) {
		handleRefreshToken();
		
		List<DriveFile> driveFiles = new ArrayList<>();
		
		try {
			String folderId = getFolderIdByName(folderNames);

			FileList result = driveService.files().list()
				      .setQ(String.format(GOOGLE_API.FOLDER_QUERY_PARENT, folderId))
				      .setSpaces(GOOGLE_API.DRIVE_SPACES)
				      .setFields(GOOGLE_API.FOLDER_QUERY_FIELDS)
				      .setPageToken(null)
				      .execute();
			
			if (result == null || !result.getFiles().isEmpty()) {
				for (File file : result.getFiles()) {
					Calendar uploadDate = Calendar.getInstance();
					uploadDate.setTimeInMillis(file.getCreatedTime().getValue());
					
					driveFiles.add(DriveFile.builder()
							.fileId(file.getId())
							.fileName(file.getName())
							.uploadDate(uploadDate)
							.build());
				}
			}
			
			return driveFiles;
			
		} catch (Exception e) {
			log.error(MSG_ERROR.GET_DRIVE_FOLDER_FILES_ERROR, e);
			return driveFiles;
		}
	}
	
	private String getFolderIdByName(List<String> folderNames) throws IOException {
		String lastFolderId = null;
		
		for (String folderName : folderNames) {
			FileList result = null;
			
			if (lastFolderId != null) {
				result = driveService.files().list()
						.setQ(String.format(GOOGLE_API.FOLDER_QUERY_NAME_PARENT, folderName, lastFolderId))
						.setSpaces(GOOGLE_API.DRIVE_SPACES)
						.setFields(GOOGLE_API.FOLDER_QUERY_FIELDS)
						.setPageToken(null)
						.execute();
			} else {
				result = driveService.files().list()
						.setQ(String.format(GOOGLE_API.FOLDER_QUERY_NAME_NO_PARENT, folderName))
						.setSpaces(GOOGLE_API.DRIVE_SPACES)
						.setFields(GOOGLE_API.FOLDER_QUERY_FIELDS)
						.setPageToken(null)
						.execute();
			}
			
			if (result == null || result.getFiles().isEmpty()) {
				lastFolderId = createFolder(folderName, lastFolderId);
			} else {
				File folder = result.getFiles().get(0);
				lastFolderId =  folder.getId();
			}
		}
		
		return lastFolderId;
	}

	private String createFolder(String folderName, String lastFolderId) throws IOException {
		File fileMetadata = new File();
		fileMetadata.setName(folderName);
		fileMetadata.setMimeType(GOOGLE_API.FOLDER_MIME_TYPE);
		
		if (lastFolderId != null && !"".equals(lastFolderId)) {
			fileMetadata.setParents(Collections.singletonList(lastFolderId));
		}

		File folder = driveService.files().create(fileMetadata)
			    .setFields(GOOGLE_API.ID_FIELD_BASE)
			    .execute();
		
		if (folder != null) {
			return folder.getId();
		}

		return null;
	}
	
}