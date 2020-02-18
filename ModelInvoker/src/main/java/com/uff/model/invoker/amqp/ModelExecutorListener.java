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
import com.uff.model.invoker.Constants.RABBIT_MQ;
import com.uff.model.invoker.domain.dto.amqp.ExecutionMessageDto;
import com.uff.model.invoker.service.ComputationalModelService;

@Service
public class ModelExecutorListener {
	
	private static final Logger log = LoggerFactory.getLogger(ModelExecutorListener.class);
	
	@Autowired
	private ComputationalModelService computationalModelService;
	
	@RabbitListener(queues = RABBIT_MQ.MODEL_EXECUTOR_QUEUE)
    public void processMessage(ExecutionMessageDto executionMessageDto,
    		Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) Long tag) throws IOException {
		
		log.info("Received message [{}]", executionMessageDto);
		
		if (executionMessageDto.getExtractorSlug() != null && 
				!"".equals(executionMessageDto.getExtractorSlug())) {
			
			computationalModelService.invokeModelTaskExtractor(executionMessageDto, channel, tag);

		} else {
			computationalModelService.invokeModelTaskExecutor(executionMessageDto, channel, tag);
		}
	}
	
}