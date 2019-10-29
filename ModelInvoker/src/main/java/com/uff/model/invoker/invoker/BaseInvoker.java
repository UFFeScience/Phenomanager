package com.uff.model.invoker.invoker;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.uff.model.invoker.Constants.BASH;
import com.uff.model.invoker.Constants.UPLOAD;
import com.uff.model.invoker.domain.ExecutionStatus;
import com.uff.model.invoker.domain.ExtractorMetadata;
import com.uff.model.invoker.domain.ModelExecutor;
import com.uff.model.invoker.domain.ModelResultMetadata;
import com.uff.model.invoker.domain.api.google.DriveFile;
import com.uff.model.invoker.exception.AbortedExecutionException;
import com.uff.model.invoker.exception.GoogleErrorApiException;
import com.uff.model.invoker.exception.ModelExecutionException;
import com.uff.model.invoker.exception.NotFoundApiException;
import com.uff.model.invoker.service.ExecutionEnvironmentService;
import com.uff.model.invoker.service.ExtractorMetadataService;
import com.uff.model.invoker.service.ModelExecutorService;
import com.uff.model.invoker.service.ModelMetadataExtractorService;
import com.uff.model.invoker.service.ModelResultMetadataService;
import com.uff.model.invoker.service.api.google.GoogleDriveService;
import com.uff.model.invoker.service.provider.CloudProviderService;
import com.uff.model.invoker.service.provider.ClusterProviderService;
import com.uff.model.invoker.service.provider.SshProviderService;
import com.uff.model.invoker.service.provider.VpnProviderService;
import com.uff.model.invoker.util.FileUtils;
import com.uff.model.invoker.util.OperationalSystemUtils;
import com.uff.model.invoker.util.OperationalSystemUtils.OS;
import com.uff.model.invoker.util.StringParserUtils;

import ch.ethz.ssh2.Connection;

@Service
public abstract class BaseInvoker {
	
	private static final Logger log = LoggerFactory.getLogger(BaseInvoker.class);
	
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
	protected ClusterProviderService clusterProviderService;
	
	@Autowired
	protected CloudProviderService cloudProviderService;
	
	@Autowired
	protected SshProviderService sshProviderService;
	
	@Autowired
	protected VpnProviderService vpnProviderService;
	
	@Lazy
	@Autowired
	protected GoogleDriveService googleDriveService;
	
	public abstract ModelResultMetadata runInSsh(ModelExecutor modelExecutor, ModelResultMetadata modelResultMetadata, Connection connection) 
					throws IOException, InterruptedException, NotFoundApiException, GoogleErrorApiException, AbortedExecutionException;
	
	public abstract ModelResultMetadata runInCluster(ModelExecutor modelExecutor, ModelResultMetadata modelResultMetadata, Connection connection) 
					throws IOException, ModelExecutionException, InterruptedException, NotFoundApiException, GoogleErrorApiException, AbortedExecutionException;
	
	public abstract ModelResultMetadata runInCloud(ModelExecutor modelExecutor, ModelResultMetadata modelResultMetadata, Connection connection) 
					throws ModelExecutionException, IOException, InterruptedException, NotFoundApiException, GoogleErrorApiException, AbortedExecutionException;
	
	protected DriveFile handleFileDownload(String fileId) throws IOException, NotFoundApiException {
		DriveFile driveFile = googleDriveService.getFileBytesContent(fileId);
		
		String temporaryFilePath = FileUtils.buildTmpPath(driveFile.getFileName());
		File temporaryFile = new File(temporaryFilePath);
		
		driveFile.setFullPath(temporaryFilePath);
		
		FileOutputStream fileOutputStream = new FileOutputStream(temporaryFile);
		fileOutputStream.write(driveFile.getFileContent());
		fileOutputStream.close();
		
		return driveFile;
	}
	
	protected String getExecutionPermissionCommand(Boolean isWorkflow, String fileName, Connection connection) {
		return getExecutionPermissionCommand(isWorkflow, fileName, null, connection);
	}
	
	public ModelResultMetadata checkExecutionExtractionStatus(ModelResultMetadata modelResultMetadata) {
		if (modelResultMetadata.getExtractorMetadatas() != null && !modelResultMetadata.getExtractorMetadatas().isEmpty()) {
			Boolean hasFailedExtraction = Boolean.FALSE;
			
			for (ExtractorMetadata extractorMetadata : modelResultMetadata.getExtractorMetadatas()) {
				if (ExecutionStatus.FAILURE.equals(extractorMetadata.getExecutionStatus())) {
					hasFailedExtraction = Boolean.TRUE;
					break;
				}
			}
			
			if (hasFailedExtraction) {
				modelResultMetadata.setExecutionStatus(ExecutionStatus.FAILURE);
			} else {
				modelResultMetadata.setExecutionStatus(ExecutionStatus.FINISHED);
			}
		}
		
		return modelResultMetadata;
	}
	
	public ModelResultMetadata handlePendingExtraction(ModelResultMetadata modelResultMetadata) {
		modelResultMetadata = modelResultMetadataService.findBySlug(modelResultMetadata.getSlug());

		if (modelResultMetadata.getExtractorMetadatas() != null && !modelResultMetadata.getExtractorMetadatas().isEmpty()) {
			for (ExtractorMetadata extractorMetadata : modelResultMetadata.getExtractorMetadatas()) {
				if (ExecutionStatus.SCHEDULED.equals(extractorMetadata.getExecutionStatus()) || 
						ExecutionStatus.RUNNING.equals(extractorMetadata.getExecutionStatus())) {
					extractorMetadata.setExecutionStatus(ExecutionStatus.ABORTED);
				}
				extractorMetadata = extractorMetadataService.update(extractorMetadata);
			}
		}
		
		return modelResultMetadata;
	}
	
	protected String getExecutionPermissionCommand(Boolean isWorkflow, String fileName, String executionCommand, Connection connection) {
		OS os = getOsTypeFromServer(connection); 
		
		if (os != null && !os.equals(OS.WINDOWS)) {
			if (isWorkflow) {
				StringBuilder formattedCommand =  new StringBuilder("chmod u+wrx ")
						.append(fileName)
						.append(" && ")
						.append("unzip ")
						.append(fileName);
				
				if (executionCommand != null && !"".equals(executionCommand)) {
					formattedCommand = formattedCommand.append(" && ").append(executionCommand);
				}
				
				return formattedCommand.toString();
				
			} else {
				StringBuilder formattedCommand =  new StringBuilder("chmod u+wrx ")
						.append(fileName);
				
				if (executionCommand != null && !"".equals(executionCommand)) {
					formattedCommand = formattedCommand.append(" && ").append(executionCommand);
				}
				
				return formattedCommand.toString();
			}
			
		} else {
			return executionCommand;
		}
	}

	private OS getOsTypeFromServer(Connection connection) {
		OS os = OS.LINUX;
		
		try {
			byte[] osNameBytes = sshProviderService.executeCommand(connection, BASH.DETECT_OS_BASH);
			String osBashName = new String(osNameBytes, StandardCharsets.UTF_8);
			os = OperationalSystemUtils.getOS(osBashName);
			
		} catch (Exception e) {
			log.error("Error while trying to deteck Operational System type", e);
		}
		
		return os;
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
	
}	