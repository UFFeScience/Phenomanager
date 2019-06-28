package com.uff.model.invoker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableAsync
@EnableScheduling
@SpringBootApplication
public class ModelInvokerApplication {
	
	@Configuration
	@Profile("default")
	@PropertySource("classpath:application.properties")
	static class DefaultEnv {}
	
	@Configuration
	@Profile("prod")
	@PropertySource({"classpath:application.properties", "classpath:application-prod.properties"})
	static class ProdEnv {}
	
	public static void main(String[] args) {
		SpringApplication.run(ModelInvokerApplication.class, args);
	}
    
}