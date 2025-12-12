package com.aneesh.suraksha.users.service;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import com.aneesh.suraksha.config.RabbitMQConfig;

@Service
public class EmailProducer {
    private final RabbitTemplate rabbitTemplate;

    public EmailProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendMail(com.aneesh.suraksha.dto.MailDto mail) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.EMAIL_EXCHANGE,
                RabbitMQConfig.EMAIL_ROUTING_KEY,
                mail);
    }
}
