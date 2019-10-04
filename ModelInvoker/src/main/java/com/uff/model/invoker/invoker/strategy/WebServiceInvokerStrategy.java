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
import com.uff.model.invoker.domain.ExecutionEnvironment;
import com.uff.model.invoker.domain.ExecutionStatus;
import com.uff.model.invoker.domain.ModelExecutor;
import com.uff.model.invoker.domain.ModelResultMetadata;
import com.uff.model.invoker.domain.WebServiceType;
import com.uff.model.invoker.domain.api.google.DriveFile;
import com.uff.model.invoker.exception.GoogleErrorApiException;
import com.uff.model.invoker.exception.ModelExecutionException;
import com.uff.model.invoker.invoker.ModelInvoker;
import com.uff.model.invoker.service.provider.VpnProviderService;

import ch.ethz.ssh2.Connection;

@Component("HttpInvokerStrategy")
public class WebServiceInvokerStrategy extends ModelInvoker {
	
	private static final Logger log = LoggerFactory.getLogger(WebServiceInvokerStrategy.class);
	
	@Autowired
	private VpnProviderService vpnProviderService;
	
	@Override
	public void startModelExecutor(ModelExecutor modelExecutor, ExecutionEnvironment executionEnvironment, 
			List<String> executionExtractors, Boolean uploadMetadata) throws Exception {
		if (modelExecutor == null) {
			log.warn("ModelExecutor not found");
			return;
		}
		
		Process vpnProcess = null;
		
		if (executionEnvironment != null) {
			vpnProcess = vpnProviderService.setupVpnConfigConection(executionEnvironment.getVpnType(), 
					modelExecutor.getComputationalModel().getId(), executionEnvironment.getVpnConfiguration());
		}
		
		if (WebServiceType.REST.equals(modelExecutor.getWebServiceType())) {
			handleRestCall(modelExecutor, executionEnvironment, uploadMetadata);
		
		} else if (WebServiceType.SOAP.equals(modelExecutor.getWebServiceType())) {
			handleSoapCall( modelExecutor, executionEnvironment, uploadMetadata);
			
		} else {
			log.warn("Unknown HTTP Protocol [{}]", modelExecutor.getWebServiceType());
		}
		
		vpnProviderService.closeVpnConnection(vpnProcess);
	}

	private void handleRestCall(ModelExecutor modelExecutor, ExecutionEnvironment executionEnvironment,
			Boolean uploadMetadata) throws IOException, GoogleErrorApiException {
		
		ModelResultMetadata modelResultMetadata = modelResultMetadataService.save(ModelResultMetadata.builder()
				.computationalModel(executionEnvironment.getComputationalModel())
    			.modelExecutor(modelExecutor)
    			.executionEnvironment(executionEnvironment)
    			.userAgent(modelExecutor.getUserAgent())
				.executionStartDate(Calendar.getInstance())
				.uploadMetadata(uploadMetadata)
				.build());
		
		HttpMethod httpMethod = HttpMethod.valueOf(modelExecutor.getHttpVerb().name());
		String url = modelExecutor.getExecutionCommand();

		modelResultMetadata = modelResultMetadataService.updateExecutionOutput(modelResultMetadata, 
				String.format("Starting execution of modelExecutor [%s] of [%s], verb [%s], body [%s], headers [%s] to url [%s]", 
				modelExecutor.getTag(), modelExecutor.getWebServiceType(), modelExecutor.getHttpVerb(), modelExecutor.getHttpBody(),
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
		
		modelResultMetadata = modelResultMetadataService
				.updateExecutionOutput(modelResultMetadata, String.format("Finished making HTTP request for modelExecutor [%s] with status [%s]", 
						modelExecutor.getTag(), response.getStatusCode().name()));
		
		if (response.getStatusCode().is2xxSuccessful()) {
			modelResultMetadata = handleUploadRestCallMetadata(modelExecutor, executionEnvironment, modelResultMetadata, responseMetadata);
			
			modelResultMetadata.setExecutionFinishDate(Calendar.getInstance());
			modelExecutor.setExecutionStatus(ExecutionStatus.IDLE);
			modelResultMetadata.setExecutorExecutionStatus(ExecutionStatus.FINISHED);
			modelResultMetadata.setExecutionStatus(ExecutionStatus.FINISHED);
			
		} else {
			modelResultMetadata = handleUploadRestCallMetadata(modelExecutor, executionEnvironment, modelResultMetadata, responseMetadata);
			
			modelResultMetadata.setExecutionFinishDate(Calendar.getInstance());
			modelExecutor.setExecutionStatus(ExecutionStatus.IDLE);
			modelResultMetadata.setExecutorExecutionStatus(ExecutionStatus.FAILURE);
			modelResultMetadata.setExecutionStatus(ExecutionStatus.FAILURE);
		}
		
		modelResultMetadataService.update(modelResultMetadata);
		modelExecutorService.update(modelExecutor);
	}

	private ModelResultMetadata handleUploadRestCallMetadata(ModelExecutor modelExecutor,
			ExecutionEnvironment executionEnvironment, ModelResultMetadata modelResultMetadata, JsonObject responseMetadata)
			throws IOException, GoogleErrorApiException {
		
		if (modelResultMetadata.getUploadMetadata() != null && modelResultMetadata.getUploadMetadata()) {
			modelResultMetadata = modelResultMetadataService
					.updateExecutionOutput(modelResultMetadata, String.format("Uploading execution metadata...", 
							executionEnvironment.getTag()));
			
			modelResultMetadata = modelResultMetadataService.update(modelResultMetadata);
			
			DriveFile driveFile = uploadMetadata(modelResultMetadata.getSlug(), modelExecutor.getTag(), responseMetadata.toString().getBytes());
			modelResultMetadata.setExecutionMetadataFileId(driveFile.getFileId());
			modelResultMetadata = modelResultMetadataService.updateExecutionOutput(modelResultMetadata, "Finished uploading execution metadata");
		}
		return modelResultMetadata;
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
	
	private void handleSoapCall(ModelExecutor modelExecutor, ExecutionEnvironment executionEnvironment,
			Boolean uploadMetadataToDrive) throws IOException, GoogleErrorApiException {
		
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
		
		modelResultMetadata = modelResultMetadataService.updateExecutionOutput(modelResultMetadata, 
				String.format("Starting execution of modelExecutor [%s] of [%s], verb [%s], body [%s], headers [%s] to url [%s] and SOAP action", 
				modelExecutor.getTag(), modelExecutor.getWebServiceType(), modelExecutor.getHttpVerb(), modelExecutor.getHttpBody(),
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
		
		modelResultMetadata = modelResultMetadataService
				.updateExecutionOutput(modelResultMetadata, String.format("Finished making HTTP request for modelExecutor [%s] with response [%s]", 
						modelExecutor.getTag(), responseString));
		
		if (uploadMetadataToDrive != null && uploadMetadataToDrive) {
			modelResultMetadata = modelResultMetadataService
					.updateExecutionOutput(modelResultMetadata, String.format("Uploading execution metadata...", 
							executionEnvironment.getTag()));
			
			DriveFile driveFile = uploadMetadata(modelResultMetadata.getSlug(), modelExecutor.getTag(), outputString.getBytes());
			modelResultMetadata.setExecutionMetadataFileId(driveFile.getFileId());
			modelResultMetadata = modelResultMetadataService.updateExecutionOutput(modelResultMetadata, "Finished uploading execution metadata");
		}
		
		modelExecutor.setExecutionStatus(ExecutionStatus.IDLE);
		modelResultMetadata.setExecutionStatus(ExecutionStatus.FINISHED);
		modelResultMetadata.setExecutionFinishDate(Calendar.getInstance());
		modelResultMetadataService.update(modelResultMetadata);
		
		modelExecutorService.update(modelExecutor);
	}
	
	@Override
	public void stopModelExecutor(ModelExecutor modelExecutor, ExecutionEnvironment executionEnvironment)
			throws IOException, ModelExecutionException, InterruptedException {
		log.info("Not allowed to STOP Web Service call. Invalid command.");
		return;
	}

	@Override
	public ModelResultMetadata runInSsh(ModelExecutor modelExecutor, ModelResultMetadata modelResultMetadata, Connection connection)
			throws IOException {
		log.info("There is no Web Service action in ssh environment");
		return modelResultMetadata;
	}

	@Override
	public ModelResultMetadata runInCloud(ModelExecutor modelExecutor, ModelResultMetadata modelResultMetadata, Connection connection)
			throws ModelExecutionException, IOException {
		log.info("There is no Web Service action in cloud environment");
		return modelResultMetadata;
	}

	@Override
	public ModelResultMetadata runInCluster(ModelExecutor modelExecutor, ModelResultMetadata modelResultMetadata, Connection connection) 
			throws IOException, ModelExecutionException {
		log.info("There is no Web Service action in cluster environment");
		return modelResultMetadata;
	}

}