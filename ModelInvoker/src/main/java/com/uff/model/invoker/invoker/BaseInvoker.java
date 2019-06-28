package com.uff.model.invoker.invoker;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.uff.model.invoker.Constants.UPLOAD;
import com.uff.model.invoker.domain.ModelExecutor;
import com.uff.model.invoker.domain.ModelResultMetadata;
import com.uff.model.invoker.domain.api.google.DriveFile;
import com.uff.model.invoker.exception.GoogleErrorApiException;
import com.uff.model.invoker.exception.ModelExecutionException;
import com.uff.model.invoker.exception.NotFoundApiException;
import com.uff.model.invoker.provider.CloudProvider;
import com.uff.model.invoker.provider.ClusterProvider;
import com.uff.model.invoker.provider.SshProvider;
import com.uff.model.invoker.provider.VpnProvider;
import com.uff.model.invoker.service.ExecutionEnvironmentService;
import com.uff.model.invoker.service.ExtractorMetadataService;
import com.uff.model.invoker.service.ModelExecutorService;
import com.uff.model.invoker.service.ModelMetadataExtractorService;
import com.uff.model.invoker.service.ModelResultMetadataService;
import com.uff.model.invoker.service.api.google.GoogleDriveService;
import com.uff.model.invoker.util.FileUtils;
import com.uff.model.invoker.util.StringParserUtils;

import ch.ethz.ssh2.Connection;

@Service
public abstract class BaseInvoker {
	
	@Autowired
	protected ExecutionEnvironmentService executionEnvironmentService;
	
	@Autowired
	protected ExtractorMetadataService extractorMetadataService;
	
	@Autowired
	protected ModelMetadataExtractorService modelMetadataExtractorService;
	
	@Autowired
	protected ModelResultMetadataService modelResultMetadataService;
	
	@Autowired
	protected ModelExecutorService modelExecutorService;
	
	@Autowired
	protected ClusterProvider clusterProvider;
	
	@Autowired
	protected CloudProvider cloudProvider;
	
	@Autowired
	protected SshProvider sshProvider;
	
	@Autowired
	protected VpnProvider vpnProvider;
	
	@Lazy
	@Autowired
	protected GoogleDriveService googleDriveService;
	
	public abstract void runInSsh(ModelExecutor modelExecutor, ModelResultMetadata modelResultMetadata, Connection connection) throws IOException, InterruptedException, NotFoundApiException, GoogleErrorApiException;
	
	public abstract void runInCluster(ModelExecutor modelExecutor, ModelResultMetadata modelResultMetadata, Connection connection) throws IOException, ModelExecutionException, InterruptedException, NotFoundApiException, GoogleErrorApiException;
	
	public abstract void runInCloud(ModelExecutor modelExecutor, ModelResultMetadata modelResultMetadata, Connection connection) throws ModelExecutionException, IOException, InterruptedException, NotFoundApiException, GoogleErrorApiException;
	
	protected String handleFileDownload(String fileId, String folderHash) throws IOException, NotFoundApiException {
		byte[] executor = googleDriveService.getFileBytesContent(fileId);
		
		File executorTempFile = File.createTempFile(folderHash, null, null);
		
		FileOutputStream fileOutputStream = new FileOutputStream(executorTempFile);
		fileOutputStream.write(executor);
		fileOutputStream.close();
		
		return executorTempFile.getName();
	}
	
	protected DriveFile uploadMetadata(String tmpHash, String folderHash, byte[] fileBytes) throws IOException, GoogleErrorApiException {
		String tmpFilePath = FileUtils.buildTmpPath(tmpHash);
		
    	FileOutputStream fos = new FileOutputStream(tmpFilePath);
    	fos.write(fileBytes);
    	fos.close();
    	
    	String parentFolderHash = new StringBuilder(
				StringParserUtils.replace(folderHash, " ", "_"))
				.append("_")
				.append(tmpHash).toString();
    	
		return googleDriveService.uploadFile(parentFolderHash, new File(tmpFilePath), 
				UPLOAD.PROFILE_IMAGES_FOLDER, Boolean.TRUE);
	}
	
	protected ModelResultMetadata updateExecutionOutput(ModelResultMetadata modelResultMetadata, String executionLog) {
		modelResultMetadata.appendExecutionLog(executionLog);
		return modelResultMetadataService.save(modelResultMetadata);
	}
	
}	