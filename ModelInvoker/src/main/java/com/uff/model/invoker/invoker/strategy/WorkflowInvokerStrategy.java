package com.uff.model.invoker.invoker.strategy;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.uff.model.invoker.domain.ExecutionStatus;
import com.uff.model.invoker.domain.ModelExecutor;
import com.uff.model.invoker.domain.ModelResultMetadata;
import com.uff.model.invoker.domain.api.google.DriveFile;
import com.uff.model.invoker.exception.GoogleErrorApiException;
import com.uff.model.invoker.exception.ModelExecutionException;
import com.uff.model.invoker.exception.NotFoundApiException;
import com.uff.model.invoker.invoker.ModelInvoker;
import com.uff.model.invoker.provider.ClusterProvider;
import com.uff.model.invoker.wrapper.LogSaverWrapper;

import ch.ethz.ssh2.Connection;

@Component("workflowInvokerStrategy")
public class WorkflowInvokerStrategy extends ModelInvoker {
	
	private static final Logger log = LoggerFactory.getLogger(WorkflowInvokerStrategy.class);
	
	@Override
	public void runInSsh(ModelExecutor modelExecutor, ModelResultMetadata modelResultMetadata, Connection connection) throws IOException, InterruptedException, NotFoundApiException, GoogleErrorApiException {
		try {
			runInCloud(modelExecutor, modelResultMetadata, connection);

		} catch (ModelExecutionException e) {
			log.error("Error while executing task in cloud environment", e);
		}
	}
	
	@Override
	public void runInCloud(ModelExecutor modelExecutor, ModelResultMetadata modelResultMetadata, Connection connection) 
			throws ModelExecutionException, IOException, InterruptedException, NotFoundApiException, GoogleErrorApiException {
		
		if (modelExecutor.getExecutorFileId() == null || "".equals(modelExecutor.getExecutorFileId())) {
			
			modelResultMetadata = updateExecutionOutput(modelResultMetadata, "No executor configured.");
			
			throw new ModelExecutionException("No executor configured");
			
		} else {
			modelResultMetadata = updateExecutionOutput(modelResultMetadata, String.format("Downloading modelExecutor [%s]...", modelExecutor.getTag()));
			
			String fileTempName = handleFileDownload(modelExecutor.getExecutorFileId(), "executor-" + 
					modelExecutor.getComputationalModel().getId());
			
			modelResultMetadata.appendExecutionLog(String.format(String.format("Finished downloading modelExecutor [%s]", 
					modelExecutor.getTag())));
			modelResultMetadata.appendExecutionLog(String.format(String.format("Uploading modelExecutor [%s] to environment...", 
					modelExecutor.getTag())));
			
			modelResultMetadata = modelResultMetadataService.save(modelResultMetadata);
			
			sshProvider.sendDataByScp(connection, fileTempName, REMOTE_MOUNT_POINT);
			StringBuilder executionCommand = new StringBuilder("unzip ")
					.append(fileTempName)
					.append(" && ")
					.append(modelExecutor.getExecutionCommand());
			
			modelResultMetadata = updateExecutionOutput(modelResultMetadata, String.format("Finished uploading modelExecutor [%s] to environment", modelExecutor.getTag()));
			
			LogSaverWrapper logSaver = LogSaverWrapper.builder()
					.modelResultMetadata(modelResultMetadata)
					.modelResultMetadataService(modelResultMetadataService).build();
			
			modelResultMetadata = updateExecutionOutput(modelResultMetadata, String.format("Executing command [%s]:", executionCommand));
			
			byte[] executionMetadata = sshProvider.executeCommand(connection, executionCommand.toString(), logSaver);
			
			modelResultMetadata = updateExecutionOutput(modelResultMetadata, String.format("Finished executing command [%s]:", executionCommand));
			
			if (executionMetadata != null) {
				modelResultMetadata = updateExecutionOutput(modelResultMetadata, "Uploading execution metadata...");
				
				DriveFile driveFile = uploadMetadata(modelResultMetadata.getSlug(), modelExecutor.getTag(), executionMetadata);
				modelResultMetadata.setExecutionMetadataFileId(driveFile.getFileId());
				
				modelResultMetadata = updateExecutionOutput(modelResultMetadata, "Finished uploading execution metadata");
			}
			
			modelExecutor.setExecutionStatus(ExecutionStatus.FINISHED);
		}
	}
	
	@Override
	public void runInCluster(ModelExecutor modelExecutor, ModelResultMetadata modelResultMetadata, Connection connection) 
			throws IOException, ModelExecutionException, InterruptedException, NotFoundApiException, GoogleErrorApiException {
		
		if (modelExecutor.getExecutorFileId() == null || "".equals(modelExecutor.getExecutorFileId()) ||
				modelExecutor.getExecutionCommand() == null) {
			
			modelResultMetadata = updateExecutionOutput(modelResultMetadata, "No executor configured.");
			
			throw new ModelExecutionException("No executor configured");
			
		} else {
			modelResultMetadata = updateExecutionOutput(modelResultMetadata, String.format("Downloading modelExecutor [%s]...", modelExecutor.getTag()));
			
			String fileTempName = handleFileDownload(modelExecutor.getExecutorFileId(), "executor-" + 
					modelExecutor.getComputationalModel().getId());
			
			modelResultMetadata.appendExecutionLog(String.format(String.format("Finished downloading modelExecutor [%s]", 
					modelExecutor.getTag())));
			modelResultMetadata.appendExecutionLog(String.format(String.format("Uploading modelExecutor [%s] to environment...", 
					modelExecutor.getTag())));
			
			modelResultMetadata = modelResultMetadataService.save(modelResultMetadata);
			
			clusterProvider.sendDataByScp(connection, fileTempName, REMOTE_MOUNT_POINT);
			
			StringBuilder configCommand = new StringBuilder("unzip ")
					.append(fileTempName);
			
			LogSaverWrapper logSaver = LogSaverWrapper.builder()
				.modelResultMetadata(modelResultMetadata)
				.modelResultMetadataService(modelResultMetadataService).build();
			
			modelResultMetadata = updateExecutionOutput(modelResultMetadata, String.format("Executing command [%s]:", modelExecutor.getExecutionCommand()));
			
			clusterProvider.executeCommand(connection, configCommand.toString(), logSaver);
			
			modelResultMetadata.appendExecutionLog(String.format("Finished executing command [%s]:", modelExecutor.getExecutionCommand()));
			modelResultMetadata.appendExecutionLog(String.format("Uploading scratch file [%s] to environment...", modelExecutor.getExecutionCommand()));
			
			modelResultMetadata = modelResultMetadataService.save(modelResultMetadata);
			
			File scratchTempFile = File.createTempFile("scratch-" + 
				modelExecutor.getComputationalModel().getId(), ClusterProvider.SCRATCH_SCRIPT_SUFIX, null);
			
			FileOutputStream fileOutputStream = new FileOutputStream(scratchTempFile);
			fileOutputStream.write(modelExecutor.getExecutionCommand().getBytes());
			fileOutputStream.close();
			
			clusterProvider.sendScriptByScp(connection, scratchTempFile.getName(),
					modelResultMetadata.getExecutionEnvironment().getUsername(), 
					modelResultMetadata.getExecutionEnvironment().getClusterName());
			
			modelResultMetadata.appendExecutionLog("Finished uploading scratch file to environment");
			modelResultMetadata.appendExecutionLog(String.format("Submiting Job [%s]:", modelExecutor.getJobName()));
			
			modelResultMetadata = modelResultMetadataService.save(modelResultMetadata);
			
			
			byte[] executionMetadata = clusterProvider.submitJob(connection, scratchTempFile.getName(), logSaver);
			
			modelResultMetadata = updateExecutionOutput(modelResultMetadata, String.format("Finished submiting Job [%s]:", modelExecutor.getJobName()));
			
			if (executionMetadata != null) {
				modelResultMetadata = updateExecutionOutput(modelResultMetadata, "Uploading execution metadata...");
				
				DriveFile driveFile = uploadMetadata(modelResultMetadata.getSlug(), modelExecutor.getTag(), executionMetadata);
				modelResultMetadata.setExecutionMetadataFileId(driveFile.getFileId());
				
				modelResultMetadata = updateExecutionOutput(modelResultMetadata, "Finished uploading execution metadata");
			}
			
			modelExecutor.setExecutionStatus(ExecutionStatus.FINISHED);
		}
            
	}

}