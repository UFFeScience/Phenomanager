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
import com.uff.model.invoker.domain.ExtractorExecution;
import com.uff.model.invoker.domain.Executor;
import com.uff.model.invoker.domain.Execution;
import com.uff.model.invoker.domain.api.google.DriveFile;
import com.uff.model.invoker.exception.AbortedExecutionException;
import com.uff.model.invoker.exception.GoogleErrorApiException;
import com.uff.model.invoker.exception.ExecutionException;
import com.uff.model.invoker.exception.NotFoundApiException;
import com.uff.model.invoker.service.EnvironmentService;
import com.uff.model.invoker.service.ExtractorExecutionService;
import com.uff.model.invoker.service.ExecutorService;
import com.uff.model.invoker.service.ExtractorService;
import com.uff.model.invoker.service.ExecutionService;
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
	protected EnvironmentService environmentService;
	
	@Autowired
	protected ExtractorExecutionService extractorExecutionService;
	
	@Autowired
	protected ExtractorService extractorService;
	
	@Autowired
	protected ExecutionService executionService;
	
	@Autowired
	protected ExecutorService executorService;
	
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
	
	public abstract Execution runInSsh(Executor executor, Execution execution, Connection connection) 
					throws IOException, InterruptedException, NotFoundApiException, GoogleErrorApiException, AbortedExecutionException;
	
	public abstract Execution runInCluster(Executor executor, Execution execution, Connection connection) 
					throws IOException, ExecutionException, InterruptedException, NotFoundApiException, GoogleErrorApiException, AbortedExecutionException;
	
	public abstract Execution runInCloud(Executor executor, Execution execution, Connection connection) 
					throws ExecutionException, IOException, InterruptedException, NotFoundApiException, GoogleErrorApiException, AbortedExecutionException;
	
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
	
	public Execution checkExtractionExecutionStatus(Execution execution) {
		if (execution.getExtractorExecutions() != null && !execution.getExtractorExecutions().isEmpty()) {
			Boolean hasFailedExtraction = Boolean.FALSE;
			
			for (ExtractorExecution extractorExecution : execution.getExtractorExecutions()) {
				if (ExecutionStatus.FAILURE.equals(extractorExecution.getStatus())) {
					hasFailedExtraction = Boolean.TRUE;
					break;
				}
			}
			
			if (hasFailedExtraction) {
				execution.setStatus(ExecutionStatus.FAILURE);
			} else {
				execution.setStatus(ExecutionStatus.FINISHED);
			}
		}
		
		return execution;
	}
	
	public Execution handlePendingExtraction(Execution execution) {
		execution = executionService.findBySlug(execution.getSlug());

		if (execution.getExtractorExecutions() != null && !execution.getExtractorExecutions().isEmpty()) {
			for (ExtractorExecution extractorExecution : execution.getExtractorExecutions()) {
				if (ExecutionStatus.SCHEDULED.equals(extractorExecution.getStatus()) || 
						ExecutionStatus.RUNNING.equals(extractorExecution.getStatus())) {
					extractorExecution.setStatus(ExecutionStatus.ABORTED);
				}
				extractorExecution = extractorExecutionService.update(extractorExecution);
			}
		}
		
		return execution;
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