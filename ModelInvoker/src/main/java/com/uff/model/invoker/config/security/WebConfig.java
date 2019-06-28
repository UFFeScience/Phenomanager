package com.uff.model.invoker.config.security;

import java.util.concurrent.Executor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.uff.model.invoker.Constants.MULTITHREAD;

@Configuration
public class WebConfig extends WebMvcConfigurerAdapter {
	
	@Value(MULTITHREAD.CORE_POOL_SIZE)
	private Integer corePoolSize;
	
	@Value(MULTITHREAD.MAX_POOL_SIZE)
	private Integer maxPoolSize;
	
	@Value(MULTITHREAD.QUEUE_CAPACITY)
	private Integer queueCapacity;
	
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