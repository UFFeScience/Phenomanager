package com.uff.model.invoker.amqp;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

import com.rabbitmq.client.Channel;
import com.uff.model.invoker.config.RabbitMqConfig;
import com.uff.model.invoker.domain.dto.amqp.ModelExecutionMessageDto;
import com.uff.model.invoker.service.ComputationalModelService;

@Service
public class ModelExecutionListener {
	
	private static final Logger log = LoggerFactory.getLogger(ModelExecutionListener.class);
	
	@Autowired
	private ComputationalModelService computationalModelService;
	
	@RabbitListener(queues = RabbitMqConfig.MODEL_EXECUTION_QUEUE)
    public void processMessage(ModelExecutionMessageDto modelExecutionMessageDto,
    		Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) Long tag) throws IOException {
		
		log.info("Received message of ComputationalModel of slug [{}]", modelExecutionMessageDto.getComputationalModelSlug());
		
		if (modelExecutionMessageDto.getModelMetadataExtractorSlug() != null && 
				!"".equals(modelExecutionMessageDto.getModelMetadataExtractorSlug())) {
			
			computationalModelService.invokeModelTaskExtractor(modelExecutionMessageDto, channel, tag);

		} else {
			computationalModelService.invokeModelTaskExecutor(modelExecutionMessageDto, channel, tag);
		}
	}
	
}