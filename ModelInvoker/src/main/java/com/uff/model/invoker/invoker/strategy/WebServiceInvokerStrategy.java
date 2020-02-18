package com.uff.model.invoker.invoker.strategy;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.google.gson.JsonObject;
import com.uff.model.invoker.domain.Environment;
import com.uff.model.invoker.domain.ExecutionStatus;
import com.uff.model.invoker.domain.Executor;
import com.uff.model.invoker.domain.Execution;
import com.uff.model.invoker.domain.User;
import com.uff.model.invoker.domain.WebServiceType;
import com.uff.model.invoker.domain.api.google.DriveFile;
import com.uff.model.invoker.exception.GoogleErrorApiException;
import com.uff.model.invoker.exception.ExecutionException;
import com.uff.model.invoker.invoker.ModelInvoker;
import com.uff.model.invoker.service.provider.VpnProviderService;

import ch.ethz.ssh2.Connection;

@Component("HttpInvokerStrategy")
public class WebServiceInvokerStrategy extends ModelInvoker {
	
	private static final Logger log = LoggerFactory.getLogger(WebServiceInvokerStrategy.class);
	
	@Autowired
	private VpnProviderService vpnProviderService;
	
	@Override
	public void startExecutor(Executor executor, Environment environment, User userAgent,
			List<String> executionExtractorSlugs, Boolean uploadMetadata) throws Exception {
		if (executor == null) {
			log.warn("Executor not found");
			return;
		}
		
		Process vpnProcess = null;
		
		if (environment != null) {
			vpnProcess = vpnProviderService.setupVpnConfigConection(environment.getVpnType(), 
					executor.getComputationalModel().getId(), environment.getVpnConfiguration());
		}
		
		if (WebServiceType.REST.equals(executor.getWebServiceType())) {
			handleRestCall(executor, environment, userAgent, uploadMetadata);
		
		} else if (WebServiceType.SOAP.equals(executor.getWebServiceType())) {
			handleSoapCall( executor, environment, userAgent, uploadMetadata);
			
		} else {
			log.warn("Unknown HTTP Protocol [{}]", executor.getWebServiceType());
		}
		
		vpnProviderService.closeVpnConnection(vpnProcess);
	}

	private void handleRestCall(Executor executor, Environment environment, User userAgent, 
			Boolean uploadMetadata) throws IOException, GoogleErrorApiException {
		
		Execution execution = executionService.save(Execution.builder()
				.computationalModel(environment.getComputationalModel())
    			.executor(executor)
    			.executorStatus(ExecutionStatus.RUNNING)
    			.environment(environment)
    			.userAgent(userAgent)
				.startDate(Calendar.getInstance())
				.uploadMetadata(uploadMetadata)
				.build());
		
		HttpMethod httpMethod = HttpMethod.valueOf(executor.getHttpVerb().name());
		String url = executor.getExecutionCommand();

		execution = executionService.updateSystemLog(execution, 
				String.format("Starting execution of Executor [%s] of [%s], verb [%s], body [%s], headers [%s] to url [%s]", 
				executor.getTag(), executor.getWebServiceType(), executor.getHttpVerb(), executor.getHttpBody(),
				executor.getHttpHeaders(), executor.getExecutionCommand()));
		
		HttpHeaders headers = new HttpHeaders();
		headers.setAll(formatHeaders(executor.getHttpHeaders()));
		
		RestTemplate restTemplate = new RestTemplate();
		HttpEntity<Object> request = new HttpEntity<>(executor.getHttpBody());
		
		ResponseEntity<Object> response = restTemplate
		  .exchange(url, httpMethod, request, Object.class);
		
		JsonObject responseMetadata = new JsonObject();
		responseMetadata.addProperty("body", response.getBody().toString());
		responseMetadata.addProperty("headers", response.getHeaders().toString());
		responseMetadata.addProperty("statusCode", response.getStatusCode().toString());
		
		execution = executionService
				.updateSystemLog(execution, String.format("Finished making HTTP request for Executor [%s] with status [%s]", 
						executor.getTag(), response.getStatusCode().name()));
		
		if (response.getStatusCode().is2xxSuccessful()) {
			execution = handleUploadRestCallMetadata(executor, environment, execution, responseMetadata);
			execution.setFinishDate(Calendar.getInstance());
			execution.setExecutorStatus(ExecutionStatus.FINISHED);
			execution.setStatus(ExecutionStatus.FINISHED);
			
		} else {
			execution = handleUploadRestCallMetadata(executor, environment, execution, responseMetadata);
			execution.setFinishDate(Calendar.getInstance());
			execution.setExecutorStatus(ExecutionStatus.FAILURE);
			execution.setStatus(ExecutionStatus.FAILURE);
		}
		
		executionService.update(execution);
		executorService.update(executor);
	}

	private Execution handleUploadRestCallMetadata(Executor executor, Environment environment, Execution execution, JsonObject responseMetadata)
			throws IOException, GoogleErrorApiException {
		
		if (execution.getUploadMetadata() != null && execution.getUploadMetadata()) {
			execution = executionService
					.updateSystemLog(execution, String.format("Uploading execution metadata...", 
							environment.getTag()));
			
			DriveFile driveFile = uploadMetadata(execution.getSlug(), executor.getTag(), responseMetadata.toString().getBytes());
			execution.setExecutionMetadataFileId(driveFile.getFileId());
			execution = executionService.updateSystemLog(execution, "Finished uploading execution metadata");
		}
		return execution;
	}

	private Map<String, String> formatHeaders(String httpHeaders) {
		Map<String, String> headerValues = new HashMap<>();
		String[] headersSplitted = httpHeaders.split(";");
		
		for (int i = 0; i < headersSplitted.length; i++) {
			String[] headerKeyValue = headersSplitted[i].split(":");
			
			if (headerKeyValue.length > 1) {
				headerValues.put(headerKeyValue[0], headerKeyValue[1]);
			}
		}
		
		return headerValues;
	}
	
	private void handleSoapCall(Executor executor, Environment environment, User userAgent,
			Boolean uploadMetadataToDrive) throws IOException, GoogleErrorApiException {
		
		Execution execution = executionService.save(Execution.builder()
			.computationalModel(environment.getComputationalModel())
			.executor(executor)
			.executorStatus(ExecutionStatus.RUNNING)
			.environment(environment)
			.userAgent(userAgent)
			.startDate(Calendar.getInstance())
			.build());
		
		String responseString = "";
		String outputString = "";
		String webserviceURL = executor.getExecutionUrl();
		
		execution = executionService.updateSystemLog(execution, 
				String.format("Starting execution of Executor [%s] of [%s], verb [%s], body [%s], headers [%s] to url [%s] and SOAP action", 
				executor.getTag(), executor.getWebServiceType(), executor.getHttpVerb(), executor.getHttpBody(),
				executor.getHttpHeaders(), executor.getExecutionUrl(), executor.getExecutionCommand()));
		
		URL url = new URL(webserviceURL);
		URLConnection connection = url.openConnection();
		HttpURLConnection httpConnection = (HttpURLConnection) connection;
		
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		String xmlInput = executor.getHttpBody();
		 
		byte[] buffer = new byte[xmlInput.length()];
		buffer = xmlInput.getBytes();
		byteArrayOutputStream.write(buffer);
		
		byte[] bytes = byteArrayOutputStream.toByteArray();
		String SOAPAction = executor.getExecutionCommand();
		
		httpConnection.setRequestMethod(executor.getHttpVerb().name());
		httpConnection.setRequestProperty("Content-Length", String.valueOf(bytes.length));
		httpConnection.setRequestProperty("SOAPAction", SOAPAction);

		Map<String, String> headers = formatHeaders(executor.getHttpHeaders());
		for (Map.Entry<String, String> header : headers.entrySet()) {
			httpConnection.setRequestProperty(header.getKey(), header.getValue());
		}
		
		httpConnection.setDoOutput(true);
		httpConnection.setDoInput(true);
		
		OutputStream outputStream = httpConnection.getOutputStream();
		outputStream.write(bytes);
		outputStream.close();
		 
		InputStreamReader inputStreamReader = new InputStreamReader(httpConnection.getInputStream());
		BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
		 
		while ((responseString = bufferedReader.readLine()) != null) {
			outputString = outputString + responseString;
		}
		
		execution = executionService
				.updateSystemLog(execution, String.format("Finished making HTTP request for Executor [%s] with response [%s]", 
						executor.getTag(), responseString));
		
		if (uploadMetadataToDrive != null && uploadMetadataToDrive) {
			execution = executionService
					.updateSystemLog(execution, String.format("Uploading execution metadata...", 
							environment.getTag()));
			
			DriveFile driveFile = uploadMetadata(execution.getSlug(), executor.getTag(), outputString.getBytes());
			execution.setExecutionMetadataFileId(driveFile.getFileId());
			execution = executionService.updateSystemLog(execution, "Finished uploading execution metadata");
		}
		
		execution.setStatus(ExecutionStatus.FINISHED);
		execution.setFinishDate(Calendar.getInstance());
		executionService.update(execution);
		
		executorService.update(executor);
	}
	
	@Override
	public void stopExecutor(Execution execution, Environment environment) throws IOException, ExecutionException, InterruptedException {
		log.info("Not allowed to STOP Web Service call. Invalid command.");
		return;
	}

	@Override
	public Execution runInSsh(Executor executor, Execution execution, Connection connection) throws IOException {
		log.info("There is no Web Service action in ssh environment");
		return execution;
	}

	@Override
	public Execution runInCloud(Executor executor, Execution execution, Connection connection) throws ExecutionException, IOException {
		log.info("There is no Web Service action in cloud environment");
		return execution;
	}

	@Override
	public Execution runInCluster(Executor executor, Execution execution, Connection connection) throws IOException, ExecutionException {
		log.info("There is no Web Service action in cluster environment");
		return execution;
	}

}