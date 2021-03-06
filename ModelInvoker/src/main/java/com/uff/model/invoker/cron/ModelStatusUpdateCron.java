package com.uff.model.invoker.cron;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.uff.model.invoker.Constants.CRON;
import com.uff.model.invoker.service.ComputationalModelService;

@Component
public class ModelStatusUpdateCron {
	
	private static final Logger log = LoggerFactory.getLogger(ModelStatusUpdateCron.class);
	
	@Autowired
	private ComputationalModelService computationalModelService;
	
	@Value(CRON.ENABLE)
	private Boolean cronEnabled; 
	
	@Scheduled(cron = CRON.PERIODICITY, zone = CRON.ZONE)
	public void processModelExecutionStatus() {
		log.info("Cron enabled: [{}]", cronEnabled);
		
		if (cronEnabled) {
			computationalModelService.updateExecutionStatus();
		}
	}
	
}