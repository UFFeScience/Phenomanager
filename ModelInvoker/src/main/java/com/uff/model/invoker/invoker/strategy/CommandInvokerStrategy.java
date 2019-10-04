package com.uff.model.invoker.invoker.strategy;

import java.io.IOException;

import org.springframework.stereotype.Component;

import com.uff.model.invoker.domain.ExecutionStatus;
import com.uff.model.invoker.domain.ModelExecutor;
import com.uff.model.invoker.domain.ModelResultMetadata;
import com.uff.model.invoker.domain.api.google.DriveFile;
import com.uff.model.invoker.exception.AbortedExecutionException;
import com.uff.model.invoker.exception.GoogleErrorApiException;
import com.uff.model.invoker.exception.ModelExecutionException;
import com.uff.model.invoker.invoker.ModelInvoker;
import com.uff.model.invoker.util.wrapper.LogSaverWrapper;

import ch.ethz.ssh2.Connection;

@Component("commandInvokerStrategy")
public class CommandInvokerStrategy extends ModelInvoker {
	
	@Override
	public ModelResultMetadata runInSsh(ModelExecutor modelExecutor, ModelResultMetadata modelResultMetadata, Connection connection) 
			throws IOException, InterruptedException, GoogleErrorApiException, AbortedExecutionException {
		
		modelResultMetadata = modelResultMetadataService.updateExecutionOutput(
				modelResultMetadata, String.format("Executing command [%s]:", modelExecutor.getExecutionCommand()));
		
		LogSaverWrapper logSaver = LogSaverWrapper.builder()
			.modelResultMetadata(modelResultMetadata)
			.modelResultMetadataService(modelResultMetadataService).build();
		
		sshProviderService.executeCommand(connection, modelExecutor.getExecutionCommand(), logSaver);
		
		modelResultMetadata = logSaver.getModelResultMetadata();
		modelResultMetadata = modelResultMetadataService.updateExecutionOutput(
				modelResultMetadata, String.format("Finished executing command [%s]", modelExecutor.getExecutionCommand()));
		
		if (logSaver.getLogOutput() != null && !"".equals(logSaver.getLogOutput()) && modelResultMetadata.getUploadMetadata() != null && modelResultMetadata.getUploadMetadata()) {
			modelResultMetadata = modelResultMetadataService.updateExecutionOutput(modelResultMetadata, "Uploading execution metadata...");
			
			DriveFile driveFile = uploadMetadata(modelResultMetadata.getSlug(), modelExecutor.getTag(), logSaver.getLogOutput().getBytes());
			modelResultMetadata.setExecutionMetadataFileId(driveFile.getFileId());
			modelResultMetadata = modelResultMetadataService.updateExecutionOutput(modelResultMetadata, "Finished uploading execution metadata");
		}
		
		modelExecutor = modelExecutorService.findBySlug(modelExecutor.getSlug());
		if (modelExecutor == null || ExecutionStatus.ABORTED.equals(modelExecutor.getExecutionStatus())) {
			throw new AbortedExecutionException("Task was aborted");
		}
		
		modelResultMetadata.setExecutorExecutionStatus(ExecutionStatus.FINISHED);
		modelExecutor.setExecutionStatus(ExecutionStatus.IDLE);
		modelResultMetadata.setModelExecutor(modelExecutorService.update(modelExecutor));
		
		return modelResultMetadataService.update(modelResultMetadata);
	}
	
	@Override
	public ModelResultMetadata runInCloud(ModelExecutor modelExecutor, ModelResultMetadata modelResultMetadata, Connection connection) 
			throws ModelExecutionException, IOException, InterruptedException, GoogleErrorApiException, AbortedExecutionException {
		return runInSsh(modelExecutor, modelResultMetadata, connection);
	}

	@Override
	public ModelResultMetadata runInCluster(ModelExecutor modelExecutor, ModelResultMetadata modelResultMetadata, Connection connection) 
			throws IOException, InterruptedException, GoogleErrorApiException, AbortedExecutionException {
		return runInSsh(modelExecutor, modelResultMetadata, connection);
	}

}