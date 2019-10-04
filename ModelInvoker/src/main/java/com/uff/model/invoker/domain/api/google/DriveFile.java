package com.uff.model.invoker.domain.api.google;

import java.util.Calendar;

public class DriveFile {
	
	private String fileId;
	private String fileName;
	private String fullPath;
	private byte[] fileContent;
	private Calendar uploadDate;
	
	public DriveFile() {}
	
	public DriveFile(DriveFileBuilder builder) {
		this.fileId = builder.fileId;
		this.fileName = builder.fileName;
		this.fullPath = builder.fullPath;
		this.fileContent = builder.fileContent;
		this.uploadDate = builder.uploadDate;
	}
	
	public String getFileId() {
		return fileId;
	}

	public void setFileId(String fileId) {
		this.fileId = fileId;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	
	public byte[] getFileContent() {
		return fileContent;
	}

	public void setFileContent(byte[] fileContent) {
		this.fileContent = fileContent;
	}
	
	public String getFullPath() {
		return fullPath;
	}

	public void setFullPath(String fullPath) {
		this.fullPath = fullPath;
	}

	public Calendar getUploadDate() {
		return uploadDate;
	}

	public void setUploadDate(Calendar uploadDate) {
		this.uploadDate = uploadDate;
	}

	public static DriveFileBuilder builder() {
		return new DriveFileBuilder();
	} 
	
	public static class DriveFileBuilder {
		
		private String fileId;
		private String fileName;
		private String fullPath;
		private byte[] fileContent;
		private Calendar uploadDate;
		
		public DriveFileBuilder fileId(String fileId) {
			this.fileId = fileId;
			return this;
		}
		
		public DriveFileBuilder fileContent(byte[] fileContent) {
			this.fileContent = fileContent;
			return this;
		}
		
		public DriveFileBuilder fileName(String fileName) {
			this.fileName = fileName;
			return this;
		}
		
		public DriveFileBuilder uploadDate(Calendar uploadDate) {
			this.uploadDate = uploadDate;
			return this;
		}
		
		public DriveFile build() {
			return new DriveFile(this);
		}
	}

	@Override
	public String toString() {
		return "DriveFile [fileId=" + fileId + ", fileName=" + fileName + ", uploadDate=" + uploadDate + "]";
	}
	
}