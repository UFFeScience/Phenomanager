package com.uff.phenomanager.repository.google;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.uff.phenomanager.Constants.GOOGLE_API;
import com.uff.phenomanager.Constants.MSG_ERROR;

@Repository
public abstract class GoogleRepository {
	
	private static final Logger log = LoggerFactory.getLogger(GoogleRepository.class);
	
	protected Credential credential;

	@Value(GOOGLE_API.REFRESH_TOKEN)
	protected String refreshToken;
	
	protected void initializeConnection() throws GeneralSecurityException, IOException {
		InputStreamReader inputStreamReader = null;
		
		try {
			JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
			HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
			inputStreamReader = new InputStreamReader(
				GoogleRepository.class.getResourceAsStream(GOOGLE_API.CLIENT_SECRET), StandardCharsets.UTF_8);
			
			GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(jsonFactory, inputStreamReader);
			credential = new GoogleCredential.Builder().setTransport(httpTransport).setJsonFactory(jsonFactory)
					.setClientSecrets(clientSecrets).build().setRefreshToken(refreshToken);
			
		} catch (Exception e) {
			throw e;
		
		} finally {
			if (inputStreamReader != null) {
				inputStreamReader.close();
			}
		}
	}
	
	protected HttpRequestInitializer setHttpTimeout(final HttpRequestInitializer requestInitializer) {
	    
		return new HttpRequestInitializer() {
	    	
	        @Override
	        public void initialize(HttpRequest httpRequest) throws IOException {
	            requestInitializer.initialize(httpRequest);
	            httpRequest.setConnectTimeout(GOOGLE_API.CONNECT_TIMEOUT);
	            httpRequest.setReadTimeout(GOOGLE_API.READ_TIMEOUT);
	        }
	    
	    };
	}
	
	protected void initializeServiceConnection() throws GeneralSecurityException, IOException {
		initializeConnection();
		initializeService();
	}
	
	protected abstract void initializeService() throws GeneralSecurityException, IOException;

	protected void handleRefreshToken() {
		try {
			if (credential == null) {
				initializeServiceConnection();
				
			} else if (credential.getExpiresInSeconds() == null || 
					  (credential.getExpiresInSeconds() != null && credential.getExpiresInSeconds() <= 0)) {
				credential.refreshToken();
			}
			
		} catch(Exception e) {
			log.error(MSG_ERROR.GOOGLE_OAUTH2_ERROR, e);
		}
	}
	
}