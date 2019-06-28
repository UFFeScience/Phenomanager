package com.uff.model.invoker.invoker.strategy;

import java.io.IOException;

import org.springframework.stereotype.Component;

import com.uff.model.invoker.domain.ExecutionStatus;
import com.uff.model.invoker.domain.ModelExecutor;
import com.uff.model.invoker.domain.ModelResultMetadata;
import com.uff.model.invoker.domain.api.google.DriveFile;
import com.uff.model.invoker.exception.GoogleErrorApiException;
import com.uff.model.invoker.exception.ModelExecutionException;
import com.uff.model.invoker.invoker.ModelInvoker;
import com.uff.model.invoker.wrapper.LogSaverWrapper;

import ch.ethz.ssh2.Connection;

@Component("commandInvokerStrategy")
public class CommandInvokerStrategy extends ModelInvoker {
	
	@Override
	public void runInSsh(ModelExecutor modelExecutor, ModelResultMetadata modelResultMetadata, Connection connection) throws IOException, InterruptedException, GoogleErrorApiException {
		modelResultMetadata = updateExecutionOutput(modelResultMetadata, String.format("Executing command [%s]:", modelExecutor.getExecutionCommand()));
		
		LogSaverWrapper logSaver = LogSaverWrapper.builder()
			.modelResultMetadata(modelResultMetadata)
			.modelResultMetadataService(modelResultMetadataService).build();
		
		byte[] executionMetadata = sshProvider.executeCommand(connection, modelExecutor.getExecutionCommand(), logSaver);
		
		modelResultMetadata = updateExecutionOutput(modelResultMetadata, String.format("Finished executing command [%s]:", modelExecutor.getExecutionCommand()));
		
		if (executionMetadata != null) {
			modelResultMetadata = updateExecutionOutput(modelResultMetadata, "Uploading execution metadata...");
			
			DriveFile driveFile = uploadMetadata(modelResultMetadata.getSlug(), modelExecutor.getTag(), executionMetadata);
			modelResultMetadata.setExecutionMetadataFileId(driveFile.getFileId());
			
			modelResultMetadata = updateExecutionOutput(modelResultMetadata, "Finished uploading execution metadata");
		}
		
		modelExecutor.setExecutionStatus(ExecutionStatus.FINISHED);
	}
	
	@Override
	public void runInCloud(ModelExecutor modelExecutor, ModelResultMetadata modelResultMetadata, Connection connection) throws ModelExecutionException, IOException, InterruptedException, GoogleErrorApiException {
		runInSsh(modelExecutor, modelResultMetadata, connection);
	}

	@Override
	public void runInCluster(ModelExecutor modelExecutor, ModelResultMetadata modelResultMetadata,
			Connection connection) throws IOException, InterruptedException, GoogleErrorApiException {
		
		runInSsh(modelExecutor, modelResultMetadata, connection);
	}

}