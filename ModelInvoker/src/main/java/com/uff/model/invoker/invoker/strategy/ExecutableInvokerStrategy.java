package com.uff.model.invoker.invoker.strategy;

import java.io.FileOutputStream;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.uff.model.invoker.Constants;
import com.uff.model.invoker.Constants.PROVIDER;
import com.uff.model.invoker.domain.Execution;
import com.uff.model.invoker.domain.Executor;
import com.uff.model.invoker.domain.api.google.DriveFile;
import com.uff.model.invoker.exception.ExecutionException;
import com.uff.model.invoker.exception.GoogleErrorApiException;
import com.uff.model.invoker.exception.NotFoundApiException;
import com.uff.model.invoker.invoker.ModelInvoker;
import com.uff.model.invoker.util.FileUtils;
import com.uff.model.invoker.util.wrapper.LogSaverWrapper;

import ch.ethz.ssh2.Connection;

@Component("executableInvokerStrategy")
public class ExecutableInvokerStrategy extends ModelInvoker {
	
	private static final Logger log = LoggerFactory.getLogger(ExecutableInvokerStrategy.class);
	
	@Override
	public Execution runInSsh(Executor executor, Execution execution, Connection connection) 
			throws IOException, InterruptedException, NotFoundApiException, GoogleErrorApiException {
		try {
			execution = runInCloud(executor, execution, connection);
		
		} catch (ExecutionException e) {
			log.error("Error while executing task in cloud environment", e);
		}
		
		return execution;
	}
	
	@Override
	public Execution runInCloud(Executor executor, Execution execution, Connection connection) 
			throws ExecutionException, IOException, InterruptedException, NotFoundApiException, GoogleErrorApiException {
		
		if (executor.getFileId() == null || "".equals(executor.getFileId())) {
			execution = executionService.updateSystemLog(execution, "No executor configured.");
			throw new ExecutionException("No executor configured");
			
		} else {
			execution = executionService.updateSystemLog(execution, String.format("Downloading modelExecutor [%s]...", executor.getTag()));
			
			DriveFile executorDriveFile = handleFileDownload(executor.getFileId());

			execution = executionService.updateSystemLog(execution,
					new String[] { String.format("Finished downloading modelExecutor [%s]", 
							executor.getTag()), 
							String.format("Uploading modelExecutor [%s] to environment...", 
									executor.getTag())});
			
			sshProviderService.sendDataByScp(connection, executorDriveFile.getFullPath(), Constants.REMOTE_MOUNT_POINT);
			execution = executionService.updateSystemLog(execution, String.format("Finished uploading modelExecutor [%s] to environment", executor.getTag()));
			
			String executionCommand = getExecutionPermissionCommand(Boolean.FALSE, executorDriveFile.getFileName(), 
					executor.getExecutionCommand(), connection);
			
			execution = executionService.updateSystemLog(execution, String.format("Executing command [%s]:", executionCommand));
			
			LogSaverWrapper logSaver = LogSaverWrapper.builder()
					.execution(execution)
					.executionService(executionService).build();
			
			sshProviderService.executeCommand(connection, executionCommand, logSaver);
			
			execution = logSaver.getExecution();
			execution = executionService.updateSystemLog(execution, String.format("Finished executing command [%s]", executor.getExecutionCommand()));
			
			if (logSaver.getLogOutput() != null && !"".equals(logSaver.getLogOutput()) && execution.getUploadMetadata() != null && execution.getUploadMetadata()) {
				execution = executionService.updateSystemLog(execution, "Uploading execution metadata...");
				
				DriveFile driveFile = uploadMetadata(execution.getSlug(), executor.getTag(), logSaver.getLogOutput().getBytes());
				execution.setExecutionMetadataFileId(driveFile.getFileId());
				execution = executionService.updateSystemLog(execution, "Finished uploading execution metadata");
			}
			
			return execution;
		}
	}
	
	@Override
	public Execution runInCluster(Executor executor, Execution execution, Connection connection) 
			throws IOException, ExecutionException, InterruptedException, NotFoundApiException, GoogleErrorApiException {
		
		if (executor.getFileId() == null || "".equals(executor.getFileId()) || 
				executor.getExecutionCommand() == null) {
			
			execution = executionService.updateSystemLog(execution, "No executor configured.");
			throw new ExecutionException("No executor configured");
			
		} else {
			execution = executionService.updateSystemLog(execution, String.format("Downloading modelExecutor [%s]...", executor.getTag()));
			
			DriveFile executorDriveFile = handleFileDownload(executor.getFileId());
			
			execution = executionService.updateSystemLog(execution,
					new String[] { String.format("Finished downloading modelExecutor [%s]", 
							executor.getTag()), 
							String.format("Uploading modelExecutor [%s] to environment...", 
									executor.getTag())});
			
			clusterProviderService.sendDataByScp(connection, executorDriveFile.getFullPath(), Constants.REMOTE_MOUNT_POINT);
			
			execution.appendSystemLog(String.format("Finished uploading modelExecutor [%s] to environment", executor.getTag()));
			execution.appendSystemLog(String.format("Uploading scratch file [%s] to environment...", executor.getTag()));
			
			execution = executionService.update(execution);
			
			String scratchFileName = PROVIDER.CLUSTER.SCRATCH_PREFIX + executor.getComputationalModel().getId() + PROVIDER.CLUSTER.SCRATCH_SCRIPT_SUFFIX;
			String scratchTempFilePath = FileUtils.buildTmpPath(scratchFileName);
			
			FileOutputStream fileOutputStream = new FileOutputStream(scratchTempFilePath);
			fileOutputStream.write(executor.getExecutionCommand().getBytes());
			fileOutputStream.close();
			
			clusterProviderService.sendScriptByScp(connection, scratchFileName,
					execution.getEnvironment().getUsername(), 
					execution.getEnvironment().getClusterName());

			execution = executionService.updateSystemLog(execution, "Finished uploading scratch file to environment");
			execution = executionService.updateSystemLog(execution, String.format("Submiting Job [%s]:", executor.getJobName()));
			
			LogSaverWrapper logSaver = LogSaverWrapper.builder()
					.execution(execution)
					.executionService(executionService).build();
			
			String permissionScratchCommand = getExecutionPermissionCommand(Boolean.FALSE, scratchFileName, connection);
			clusterProviderService.executeCommand(connection, permissionScratchCommand, logSaver);
			
			byte[] executionMetadata = clusterProviderService.submitJob(connection, scratchFileName, logSaver);
			
			execution = logSaver.getExecution();
			execution = executionService.updateSystemLog(execution, String.format("Finished submiting Job [%s]:", executor.getJobName()));
			
			if (executionMetadata != null && executionMetadata.length > 0 && execution.getUploadMetadata() != null && execution.getUploadMetadata()) {
				execution = executionService.updateSystemLog(execution, "Uploading execution metadata...");
				
				DriveFile driveFile = uploadMetadata(execution.getSlug(), executor.getTag(), executionMetadata);
				execution.setExecutionMetadataFileId(driveFile.getFileId());
				execution = executionService.updateSystemLog(execution, "Finished uploading execution metadata");
			}
		}
		
		return execution;
	}

}