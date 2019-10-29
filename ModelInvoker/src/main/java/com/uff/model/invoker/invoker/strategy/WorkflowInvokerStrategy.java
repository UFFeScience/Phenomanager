package com.uff.model.invoker.invoker.strategy;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.uff.model.invoker.domain.ModelExecutor;
import com.uff.model.invoker.domain.ModelResultMetadata;
import com.uff.model.invoker.domain.api.google.DriveFile;
import com.uff.model.invoker.exception.GoogleErrorApiException;
import com.uff.model.invoker.exception.ModelExecutionException;
import com.uff.model.invoker.exception.NotFoundApiException;
import com.uff.model.invoker.invoker.ModelInvoker;
import com.uff.model.invoker.service.provider.ClusterProviderService;
import com.uff.model.invoker.util.FileUtils;
import com.uff.model.invoker.util.wrapper.LogSaverWrapper;

import ch.ethz.ssh2.Connection;

@Component("workflowInvokerStrategy")
public class WorkflowInvokerStrategy extends ModelInvoker {
	
	private static final Logger log = LoggerFactory.getLogger(WorkflowInvokerStrategy.class);
	
	@Override
	public ModelResultMetadata runInSsh(ModelExecutor modelExecutor, ModelResultMetadata modelResultMetadata, Connection connection) 
			throws IOException, InterruptedException, NotFoundApiException, GoogleErrorApiException {
		try {
			modelResultMetadata = runInCloud(modelExecutor, modelResultMetadata, connection);
		} catch (ModelExecutionException e) {
			log.error("Error while executing task in cloud environment", e);
		}
		
		return modelResultMetadata;
	}
	
	@Override
	public ModelResultMetadata runInCloud(ModelExecutor modelExecutor, ModelResultMetadata modelResultMetadata, Connection connection) 
			throws ModelExecutionException, IOException, InterruptedException, NotFoundApiException, GoogleErrorApiException {
		
		if (modelExecutor.getExecutorFileId() == null || "".equals(modelExecutor.getExecutorFileId())) {
			modelResultMetadata = modelResultMetadataService.updateSystemLog(modelResultMetadata, "No executor configured.");
			throw new ModelExecutionException("No executor configured");
			
		} else {
			modelResultMetadata = modelResultMetadataService.updateSystemLog(modelResultMetadata, String.format("Downloading modelExecutor [%s]...", modelExecutor.getTag()));
			
			DriveFile executorDriveFile = handleFileDownload(modelExecutor.getExecutorFileId());
			
			modelResultMetadata = modelResultMetadataService.updateSystemLog(modelResultMetadata,
					new String[] { String.format("Finished downloading modelExecutor [%s]", 
							modelExecutor.getTag()), 
							String.format("Uploading modelExecutor [%s] to environment...", 
									modelExecutor.getTag())});
			
			sshProviderService.sendDataByScp(connection, executorDriveFile.getFullPath(), REMOTE_MOUNT_POINT);
			String executionCommand = getExecutionPermissionCommand(Boolean.TRUE, executorDriveFile.getFileName(), 
					modelExecutor.getExecutionCommand(), connection);
			
			modelResultMetadata = modelResultMetadataService
					.updateSystemLog(modelResultMetadata, String.format("Finished uploading modelExecutor [%s] to environment", modelExecutor.getTag()));
			
			modelResultMetadata = modelResultMetadataService
					.updateSystemLog(modelResultMetadata, String.format("Executing command [%s]:", executionCommand));
			
			LogSaverWrapper logSaver = LogSaverWrapper.builder()
					.modelResultMetadata(modelResultMetadata)
					.modelResultMetadataService(modelResultMetadataService).build();
			
			sshProviderService.executeCommand(connection, executionCommand.toString(), logSaver);
			
			modelResultMetadata = logSaver.getModelResultMetadata();
			modelResultMetadata = modelResultMetadataService
					.updateSystemLog(modelResultMetadata, String.format("Finished executing command [%s]", executionCommand));
			
			if (logSaver.getLogOutput() != null && !"".equals(logSaver.getLogOutput()) && modelResultMetadata.getUploadMetadata() != null && modelResultMetadata.getUploadMetadata()) {
				modelResultMetadata = modelResultMetadataService
						.updateSystemLog(modelResultMetadata, "Uploading execution metadata...");
				
				DriveFile driveFile = uploadMetadata(modelResultMetadata.getSlug(), modelExecutor.getTag(), logSaver.getLogOutput().getBytes());
				modelResultMetadata.setExecutionMetadataFileId(driveFile.getFileId());
				modelResultMetadata = modelResultMetadataService
						.updateSystemLog(modelResultMetadata, "Finished uploading execution metadata");
			}
			
			return modelResultMetadata;
		}
	}
	
	@Override
	public ModelResultMetadata runInCluster(ModelExecutor modelExecutor, ModelResultMetadata modelResultMetadata, Connection connection) 
			throws IOException, ModelExecutionException, InterruptedException, NotFoundApiException, GoogleErrorApiException {
		
		if (modelExecutor.getExecutorFileId() == null || "".equals(modelExecutor.getExecutorFileId()) ||
				modelExecutor.getExecutionCommand() == null) {
			
			modelResultMetadata = modelResultMetadataService.updateSystemLog(modelResultMetadata, "No executor configured.");
			throw new ModelExecutionException("No executor configured");
			
		} else {
			modelResultMetadata = modelResultMetadataService
					.updateSystemLog(modelResultMetadata, String.format("Downloading modelExecutor [%s]...", modelExecutor.getTag()));
			
			DriveFile executorDriveFile = handleFileDownload(modelExecutor.getExecutorFileId());
			
			modelResultMetadata = modelResultMetadataService.updateSystemLog(modelResultMetadata,
					new String[] { String.format("Finished downloading modelExecutor [%s]", 
							modelExecutor.getTag()), 
							String.format("Uploading modelExecutor [%s] to environment...", 
									modelExecutor.getTag())});
			
			clusterProviderService.sendDataByScp(connection, executorDriveFile.getFullPath(), REMOTE_MOUNT_POINT);
			
			String configCommand = getExecutionPermissionCommand(Boolean.TRUE, executorDriveFile.getFileName(), connection);
			
			modelResultMetadata = modelResultMetadataService
					.updateSystemLog(modelResultMetadata, String.format("Executing command [%s]:", modelExecutor.getExecutionCommand()));
			
			LogSaverWrapper logSaver = LogSaverWrapper.builder()
					.modelResultMetadata(modelResultMetadata)
					.modelResultMetadataService(modelResultMetadataService).build();
			
			clusterProviderService.executeCommand(connection, configCommand, logSaver);
			
			modelResultMetadata = logSaver.getModelResultMetadata();
			modelResultMetadata.appendSystemLog(String.format("Finished executing command [%s]", modelExecutor.getExecutionCommand()));
			modelResultMetadata.appendSystemLog(String.format("Uploading scratch file [%s] to environment...", modelExecutor.getExecutionCommand()));
			modelResultMetadata = modelResultMetadataService.update(modelResultMetadata);
			
			String scratchFileName = "scratch-" + modelExecutor.getComputationalModel().getId() + ClusterProviderService.SCRATCH_SCRIPT_SUFIX;
			String scratchTempFilePath = FileUtils.buildTmpPath(scratchFileName);
			
			FileOutputStream fileOutputStream = new FileOutputStream(new File(scratchTempFilePath));
			fileOutputStream.write(modelExecutor.getExecutionCommand().getBytes());
			fileOutputStream.close();
			
			clusterProviderService.sendScriptByScp(connection, scratchTempFilePath,
					modelResultMetadata.getExecutionEnvironment().getUsername(), 
					modelResultMetadata.getExecutionEnvironment().getClusterName());
			
			modelResultMetadata.appendSystemLog("Finished uploading scratch file to environment");
			modelResultMetadata.appendSystemLog(String.format("Submiting Job [%s]:", modelExecutor.getJobName()));
			modelResultMetadata = modelResultMetadataService.update(modelResultMetadata);
			
			modelResultMetadata = modelResultMetadataService
					.updateSystemLog(modelResultMetadata, String.format("Executing command [%s]:", modelExecutor.getExecutionCommand()));
			logSaver.setModelResultMetadata(modelResultMetadata);
			
			String permissionScratchCommand = getExecutionPermissionCommand(Boolean.FALSE, scratchFileName, connection);
			clusterProviderService.executeCommand(connection, permissionScratchCommand, logSaver);
			byte[] executionMetadata = clusterProviderService.submitJob(connection, scratchTempFilePath, logSaver);
			
			modelResultMetadata = logSaver.getModelResultMetadata();
			modelResultMetadata = modelResultMetadataService
					.updateSystemLog(modelResultMetadata, String.format("Finished submiting Job [%s]:", modelExecutor.getJobName()));
			
			if (executionMetadata != null && executionMetadata.length > 0 && modelResultMetadata.getUploadMetadata() != null && 
					modelResultMetadata.getUploadMetadata()) {
				modelResultMetadata = modelResultMetadataService
						.updateSystemLog(modelResultMetadata, "Uploading execution metadata...");
				
				DriveFile driveFile = uploadMetadata(modelResultMetadata.getSlug(), modelExecutor.getTag(), executionMetadata);
				modelResultMetadata.setExecutionMetadataFileId(driveFile.getFileId());
				modelResultMetadata = modelResultMetadataService
						.updateSystemLog(modelResultMetadata, "Finished uploading execution metadata");
			}
			
			return modelResultMetadata;
		}
	}

}