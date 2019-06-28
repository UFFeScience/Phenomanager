package com.uff.model.invoker.provider;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.uff.model.invoker.domain.JobStatus;
import com.uff.model.invoker.wrapper.LogSaverWrapper;

import ch.ethz.ssh2.Connection;

@Component
public class ClusterProvider {
	
	private static final String STATUS_REASON_COLUMN = "ST";
	private static final String SCRATCH_DIRECTORY = "/scratch";
	private static final String SCRATCH_SCRIPT_COMMAND = "sbatch ";
	private static final String SCRATCH_CHECK_STATUS_COMMAND = "squeue -n ";
	private static final String SCRATCH_STOP_COMMAND = "scancel -n ";
	public static final String SCRATCH_SCRIPT_SUFIX = ".srm";

	@Autowired
	private SshProvider sshProvider;
	
	public Connection openEnvironmentConnection(String hostAddress, String username, String password) throws Exception {
		return sshProvider.openEnvironmentConnection(hostAddress, username, password);
	}
	
	public byte[] executeCommand(Connection connection, String executionCommand, LogSaverWrapper logSaver) throws IOException, InterruptedException {
        return sshProvider.executeCommand(connection, executionCommand, logSaver);
	}
	
	public byte[] submitJob(Connection connection, String scratchScriptFileName, LogSaverWrapper logSaver) throws IOException, InterruptedException {
        return executeCommand(connection, SCRATCH_SCRIPT_COMMAND + scratchScriptFileName, logSaver);
	}
	
	public byte[] stopJob(Connection connection, String jobName) throws IOException, InterruptedException {
        return executeCommand(connection, SCRATCH_STOP_COMMAND + jobName, null);
	}
	
	public JobStatus checkJobStatus(Connection connection, String jobName) throws IOException, InterruptedException {
		Map<String, String> jobData = parseJobData(executeCommand(connection, SCRATCH_CHECK_STATUS_COMMAND + jobName, null));
        return JobStatus.getJobStatusFromString(jobData.get(STATUS_REASON_COLUMN));
	}
	
	private Map<String, String> parseJobData(byte[] jobStatusOutputBytes) {
		String jobStatusOutput = new String(jobStatusOutputBytes, StandardCharsets.UTF_8);
		Map<String, String> jobData = new HashMap<>();
		
		if (jobStatusOutput == null || "".equals(jobStatusOutput)) {
			return jobData;
		}
		
		String[] lines = jobStatusOutput.split("\n");

		if (lines.length <= 1) {
			return jobData;
		}
		
		String[] headers = lines[0].split(" ");
		String[] data = lines[1].split(" ");
		
		for (int i = 0; i < headers.length; i++) {
			jobData.put(headers[i].trim(), data[i].trim());
		}
		
		return jobData;
	}

	public void sendDataByScp(Connection connection, String fileName, String remoteMountPoint) throws IOException {
		sshProvider.sendDataByScp(connection, fileName, remoteMountPoint);
    }
	
	public void sendScriptByScp(Connection connection, String fileName, String username, String clusterProjectName) throws IOException {
		StringBuilder scratchPath = new StringBuilder(SCRATCH_DIRECTORY);
		scratchPath.append("/");
		scratchPath.append(clusterProjectName);
		scratchPath.append("/");
		scratchPath.append(username);
		
		sshProvider.sendDataByScp(connection, fileName, scratchPath.toString());
    }
    
}