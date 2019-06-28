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
import com.uff.model.invoker.domain.ExecutionEnvironment;
import com.uff.model.invoker.domain.ExecutionStatus;
import com.uff.model.invoker.domain.HttpProtocolType;
import com.uff.model.invoker.domain.ModelExecutor;
import com.uff.model.invoker.domain.ModelResultMetadata;
import com.uff.model.invoker.domain.api.google.DriveFile;
import com.uff.model.invoker.exception.GoogleErrorApiException;
import com.uff.model.invoker.exception.ModelExecutionException;
import com.uff.model.invoker.invoker.ModelInvoker;
import com.uff.model.invoker.provider.VpnProvider;
import com.uff.model.invoker.service.ExecutionEnvironmentService;

import ch.ethz.ssh2.Connection;

@Component("HttpInvokerStrategy")
public class HttpInvokerStrategy extends ModelInvoker {
	
	private static final Logger log = LoggerFactory.getLogger(HttpInvokerStrategy.class);
	
	@Autowired
	private ExecutionEnvironmentService executionEnvironmentService;
	
	@Autowired
	private VpnProvider vpnProvider;
	
	@Override
	public void startModelExecutor(ModelExecutor modelExecutor) throws Exception {
		if (modelExecutor == null) {
			log.warn("ModelExecutor not found");
			return;
		}
		
		ExecutionEnvironment executionEnvironment = executionEnvironmentService
				.findByComputationalModelAndActive(modelExecutor.getComputationalModel(), Boolean.TRUE);
		
		Process vpnProcess = null;
		
		if (executionEnvironment != null) {
			vpnProcess = vpnProvider.setupVpnConfigConection(executionEnvironment.getVpnType(), 
					modelExecutor.getComputationalModel().getId(), executionEnvironment.getVpnConfiguration());
		}
		
		if (HttpProtocolType.REST.equals(modelExecutor.getHttpProtocolType())) {
			handleRestCall(modelExecutor, executionEnvironment);
		
		} else if (HttpProtocolType.SOAP.equals(modelExecutor.getHttpProtocolType())) {
			handleSoapCall( modelExecutor, executionEnvironment);
			
		} else {
			log.warn("Unknown HTTP Protocol [{}]", modelExecutor.getHttpProtocolType());
		}
		
		vpnProvider.closeVpnConnection(vpnProcess);
	}

	private void handleRestCall(ModelExecutor modelExecutor, ExecutionEnvironment executionEnvironment) throws IOException, GoogleErrorApiException {
		ModelResultMetadata modelResultMetadata = modelResultMetadataService.save(ModelResultMetadata.builder()
				.computationalModel(executionEnvironment.getComputationalModel())
    			.modelExecutor(modelExecutor)
    			.executionEnvironment(executionEnvironment)
    			.userAgent(modelExecutor.getUserAgent())
				.executionStartDate(Calendar.getInstance())
				.build());
		
		HttpMethod httpMethod = HttpMethod.valueOf(modelExecutor.getHttpVerb().name());
		String url = modelExecutor.getExecutionCommand();

		modelResultMetadata = updateExecutionOutput(modelResultMetadata, 
				String.format("Starting execution of modelExecutor [%s] of [%s], verb [%s], body [%s], headers [%s] to url [%s]", 
				modelExecutor.getTag(), modelExecutor.getHttpProtocolType(), modelExecutor.getHttpVerb(), modelExecutor.getHttpBody(),
				modelExecutor.getHttpHeaders(), modelExecutor.getExecutionCommand()));
		
		HttpHeaders headers = new HttpHeaders();
		headers.setAll(formatHeaders(modelExecutor.getHttpHeaders()));
		
		RestTemplate restTemplate = new RestTemplate();
		HttpEntity<Object> request = new HttpEntity<>(modelExecutor.getHttpBody());
		
		ResponseEntity<Object> response = restTemplate
		  .exchange(url, httpMethod, request, Object.class);
		
		JsonObject responseMetadata = new JsonObject();
		responseMetadata.addProperty("body", response.getBody().toString());
		responseMetadata.addProperty("headers", response.getHeaders().toString());
		responseMetadata.addProperty("statusCode", response.getStatusCode().toString());

		modelExecutor.setExecutionStatus(ExecutionStatus.FINISHED);
		
		if (response.getStatusCode().is2xxSuccessful()) {
			modelResultMetadata.setExecutionStatus(ExecutionStatus.FINISHED);
		} else {
			modelResultMetadata.setExecutionStatus(ExecutionStatus.FAILURE);
		}
		
		modelResultMetadata.appendExecutionLog(String.format("Finished making HTTP request for modelExecutor [%s] with status [%s]", 
				modelExecutor.getTag(), response.getStatusCode().name()));
		modelResultMetadata.appendExecutionLog(String.format("Uploading execution metadata...", 
				executionEnvironment.getTag()));
		
		modelResultMetadata = modelResultMetadataService.save(modelResultMetadata);
		
		DriveFile driveFile = uploadMetadata(modelResultMetadata.getSlug(), modelExecutor.getTag(), responseMetadata.toString().getBytes());
		modelResultMetadata.setExecutionMetadataFileId(driveFile.getFileId());
		modelResultMetadata.setExecutionFinishDate(Calendar.getInstance());
		
		modelResultMetadata = updateExecutionOutput(modelResultMetadata, "Finished uploading execution metadata");
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
	
	private void handleSoapCall(ModelExecutor modelExecutor, ExecutionEnvironment executionEnvironment) throws IOException, GoogleErrorApiException {
		ModelResultMetadata modelResultMetadata = modelResultMetadataService.save(ModelResultMetadata.builder()
				.computationalModel(executionEnvironment.getComputationalModel())
    			.modelExecutor(modelExecutor)
    			.executionEnvironment(executionEnvironment)
    			.userAgent(modelExecutor.getUserAgent())
				.executionStartDate(Calendar.getInstance())
				.build());
		
		String responseString = "";
		String outputString = "";
		String webserviceURL = modelExecutor.getExecutionUrl();
		
		modelResultMetadata = updateExecutionOutput(modelResultMetadata, 
				String.format("Starting execution of modelExecutor [%s] of [%s], verb [%s], body [%s], headers [%s] to url [%s] and SOAP action", 
				modelExecutor.getTag(), modelExecutor.getHttpProtocolType(), modelExecutor.getHttpVerb(), modelExecutor.getHttpBody(),
				modelExecutor.getHttpHeaders(), modelExecutor.getExecutionUrl(), modelExecutor.getExecutionCommand()));
		
		URL url = new URL(webserviceURL);
		URLConnection connection = url.openConnection();
		HttpURLConnection httpConnection = (HttpURLConnection) connection;
		
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		String xmlInput = modelExecutor.getHttpBody();
		 
		byte[] buffer = new byte[xmlInput.length()];
		buffer = xmlInput.getBytes();
		byteArrayOutputStream.write(buffer);
		
		byte[] bytes = byteArrayOutputStream.toByteArray();
		String SOAPAction = modelExecutor.getExecutionCommand();
		
		httpConnection.setRequestMethod(modelExecutor.getHttpVerb().name());
		httpConnection.setRequestProperty("Content-Length", String.valueOf(bytes.length));
		httpConnection.setRequestProperty("SOAPAction", SOAPAction);

		Map<String, String> headers = formatHeaders(modelExecutor.getHttpHeaders());
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
		
		modelExecutor.setExecutionStatus(ExecutionStatus.FINISHED);
		modelResultMetadata.setExecutionStatus(ExecutionStatus.FINISHED);
		
		modelResultMetadata.appendExecutionLog(String.format("Finished making HTTP request for modelExecutor [%s] with response [%s]", 
				modelExecutor.getTag(), responseString));
		modelResultMetadata.appendExecutionLog(String.format("Uploading execution metadata...", 
				executionEnvironment.getTag()));
		
		modelResultMetadata = modelResultMetadataService.save(modelResultMetadata);
		
		DriveFile driveFile = uploadMetadata(modelResultMetadata.getSlug(), modelExecutor.getTag(), outputString.getBytes());
		modelResultMetadata.setExecutionMetadataFileId(driveFile.getFileId());
		modelResultMetadata.setExecutionFinishDate(Calendar.getInstance());
		
		modelResultMetadata = updateExecutionOutput(modelResultMetadata, "Finished uploading execution metadata");
	}
	
	@Override
	public void stopModelExecutor(ModelExecutor modelExecutor)
			throws IOException, ModelExecutionException, InterruptedException {
		
		log.info("Not allowed to STOP HTTP call. Invalid command.");
		return;
	}

	@Override
	public void runInSsh(ModelExecutor modelExecutor, ModelResultMetadata modelResultMetadata, Connection connection)
			throws IOException {
		
		log.info("There is no HTTP action in ssh environment");
		return;
	}

	@Override
	public void runInCloud(ModelExecutor modelExecutor, ModelResultMetadata modelResultMetadata, Connection connection)
			throws ModelExecutionException, IOException {
		
		log.info("There is no HTTP action in cloud environment");
		return;
	}

	@Override
	public void runInCluster(ModelExecutor modelExecutor, ModelResultMetadata modelResultMetadata,
			Connection connection) throws IOException, ModelExecutionException {
		
		log.info("There is no HTTP action in cluster environment");
		return;
	}

}