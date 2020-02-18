package com.uff.model.invoker.invoker.strategy;

import java.io.IOException;

import org.springframework.stereotype.Component;

import com.uff.model.invoker.domain.Executor;
import com.uff.model.invoker.domain.Execution;
import com.uff.model.invoker.domain.api.google.DriveFile;
import com.uff.model.invoker.exception.AbortedExecutionException;
import com.uff.model.invoker.exception.GoogleErrorApiException;
import com.uff.model.invoker.exception.ExecutionException;
import com.uff.model.invoker.invoker.ModelInvoker;
import com.uff.model.invoker.util.wrapper.LogSaverWrapper;

import ch.ethz.ssh2.Connection;

@Component("commandInvokerStrategy")
public class CommandInvokerStrategy extends ModelInvoker {
	
	@Override
	public Execution runInSsh(Executor executor, Execution execution, Connection connection) 
			throws IOException, InterruptedException, GoogleErrorApiException, AbortedExecutionException {
		
		execution = executionService.updateSystemLog(execution, String.format("Executing command [%s]:", executor.getExecutionCommand()));
		
		LogSaverWrapper logSaver = LogSaverWrapper.builder()
			.execution(execution)
			.executionService(executionService).build();
		
		sshProviderService.executeCommand(connection, executor.getExecutionCommand(), logSaver);
		
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
	
	@Override
	public Execution runInCloud(Executor executor, Execution execution, Connection connection) 
			throws ExecutionException, IOException, InterruptedException, GoogleErrorApiException, AbortedExecutionException {
		return runInSsh(executor, execution, connection);
	}

	@Override
	public Execution runInCluster(Executor executor, Execution execution, Connection connection) 
			throws IOException, InterruptedException, GoogleErrorApiException, AbortedExecutionException {
		return runInSsh(executor, execution, connection);
	}

}