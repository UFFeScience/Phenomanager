package com.uff.phenomanager.amqp;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.uff.phenomanager.Constants;
import com.uff.phenomanager.domain.dto.amqp.ExecutionMessageDto;

@Service
public class ModelExecutorSender {
	
	private static final Logger log = LoggerFactory.getLogger(ModelExecutorSender.class);
	
	@Autowired
	private RabbitTemplate rabbitTemplate;

	@Autowired
	private AmqpAdmin amqpAdmin;

	@PostConstruct
	public void setUpQueue() {
		this.amqpAdmin.declareQueue(new Queue(Constants.RABBIT_MQ.MODEL_EXECUTOR_QUEUE));
	}

	@Async
	public void sendMessage(ExecutionMessageDto executionMessageDTO) {
		log.info("Sending message [{}] for execution", executionMessageDTO);
		this.rabbitTemplate.convertAndSend(Constants.RABBIT_MQ.MODEL_EXECUTOR_QUEUE, executionMessageDTO);
	}
	
}