package com.uff.phenomanager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;

@SpringBootApplication
public class PhenoManagerApiApplication {
	
	@Configuration
	@Profile("default")
	@PropertySource("classpath:application.properties")
	static class DefaultEnv {}
	
	@Configuration
	@Profile("prod")
	@PropertySource({"classpath:application.properties", "classpath:application-prod.properties"})
	static class ProdEnv {}
	
	public static void main(String[] args) {
		SpringApplication.run(PhenoManagerApiApplication.class, args);
	}

}