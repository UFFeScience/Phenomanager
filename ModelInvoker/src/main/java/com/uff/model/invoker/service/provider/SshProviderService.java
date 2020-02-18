package com.uff.model.invoker.service.provider;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.uff.model.invoker.Constants.PROVIDER;
import com.uff.model.invoker.util.wrapper.LogSaverWrapper;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.SCPClient;
import ch.ethz.ssh2.Session;

@Component
public class SshProviderService {
	
	private static final Logger log = LoggerFactory.getLogger(SshProviderService.class);

	public Connection openEnvironmentConnection(String hostAddress, String username, String password) throws Exception {
		Connection connection = new Connection(hostAddress, PROVIDER.SSH.DEFAULT_PORT);
		connection.connect();

		Boolean isAuthenticated = connection.authenticateWithPassword(username, password);
		if (isAuthenticated == false) {
			throw new IOException("Authentication failed");
		}
		
		return connection;
	}
	
	public byte[] executeCommand(Connection connection, String executionCommand) throws IOException, InterruptedException {
		InputStream inputStream = null;
		Session session = null;
		byte[] commandOutput = null;
		
        try {
    		session = connection.openSession();
			session.execCommand(executionCommand);
			
			waitCommandCompletion(session, null);
			
			inputStream = session.getStdout();
			commandOutput = readCommandOutput(inputStream);
			
        } catch (IOException e) {
			log.error("Error while executing comand [{}]", executionCommand, e);
		
        } finally {
        	if (session != null) {
        		session.close();
        	}
        }
        
        return commandOutput;
	}
	
	public byte[] executeCommand(Connection connection, String executionCommand, LogSaverWrapper logSaverWrapper) throws IOException, InterruptedException {
		InputStream inputStream = null;
		Session session = null;
		byte[] commandOutput = null;
		
        try {
    		session = connection.openSession();
			session.execCommand(executionCommand);
			
			waitCommandCompletion(session, logSaverWrapper);
			
			inputStream = session.getStdout();
			commandOutput = readCommandOutput(inputStream);
			
        } catch (IOException e) {
			log.error("Error while executing comand [{}]", executionCommand, e);
		
        } finally {
        	if (session != null) {
        		session.close();
        	}
        }
        
        return commandOutput;
	}

	private byte[] readCommandOutput(InputStream inputStream) throws IOException {
		if (inputStream == null) {
			log.warn("Null output");
			return null;
		}
		
		ByteArrayOutputStream result = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		Integer length;
		
		while ((length = inputStream.read(buffer)) != -1) {
		    result.write(buffer, 0, length);
		}

		return result.toByteArray();
		
	}
	
	public void sendDataByScp(Connection connection, String fileName, String remoteMountPoint) throws IOException {
        SCPClient scp = new SCPClient(connection);
        scp.put(fileName, remoteMountPoint);
    }
    
    public void getDataByScp(Connection connection, String filePath, String localPoint) throws IOException {
        SCPClient scp = new SCPClient(connection);
        scp.get(filePath, localPoint);
    }
    
    private void waitCommandCompletion(Session session, LogSaverWrapper logSaverWrapper) throws InterruptedException, IOException {
    	processCommandStdout(session, logSaverWrapper);
		Integer exitStatus = session.getExitStatus();
		
		while (exitStatus == null) {
		    exitStatus = session.getExitStatus();
		    sleepThread(5);
		    processCommandStdout(session, logSaverWrapper);
		}
		
		processCommandStdout(session, logSaverWrapper);
	}

	public void sleepThread(Integer secondsToSleep) throws InterruptedException {
        Thread.sleep(secondsToSleep * PROVIDER.SSH.WAIT_TIME);
    }
	
	private void processCommandStdout(Session session, LogSaverWrapper logSaverWrapper) throws IOException {
		if (logSaverWrapper != null) {
			logSaverWrapper.updateLog(new String(readCommandOutput(session.getStdout()), StandardCharsets.UTF_8));
		}
	}
	
}