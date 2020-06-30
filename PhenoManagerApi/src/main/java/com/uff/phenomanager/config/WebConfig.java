package com.uff.phenomanager.config;

import java.util.TimeZone;
import java.util.concurrent.Executor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.uff.phenomanager.Constants.JWT_AUTH;
import com.uff.phenomanager.Constants.MULTITHREAD;

@Configuration
public class WebConfig extends WebMvcConfigurerAdapter {
	
	@Value(MULTITHREAD.CORE_POOL_SIZE)
	private Integer corePoolSize;
	
	@Value(MULTITHREAD.MAX_POOL_SIZE)
	private Integer maxPoolSize;
	
	@Value(MULTITHREAD.QUEUE_CAPACITY)
	private Integer queueCapacity;
	
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping(JWT_AUTH.ALL_PATH_CORS_REGEX)
                .allowedMethods(
                		HttpMethod.HEAD.name(), 
                		HttpMethod.OPTIONS.name(), 
                		HttpMethod.GET.name(), 
                		HttpMethod.PUT.name(), 
                		HttpMethod.POST.name(), 
                		HttpMethod.DELETE.name(), 
                		HttpMethod.PATCH.name());
    }
    
    @Bean
	public Jackson2ObjectMapperBuilderCustomizer jacksonObjectMapperCustomization() {
        return new Jackson2ObjectMapperBuilderCustomizer() {

            @Override
            public void customize(Jackson2ObjectMapperBuilder jacksonObjectMapperBuilder) {
            	jacksonObjectMapperBuilder.timeZone(TimeZone.getDefault());
            }

        };
	}
    
    @Bean
    public Executor asyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.initialize();

        return executor;
    }

}