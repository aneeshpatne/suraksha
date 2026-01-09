package com.aneesh.suraksha.users.service;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import com.aneesh.suraksha.config.RabbitMQConfig;
import com.aneesh.suraksha.dto.MailDto;

@Service
public class MailSenderService {

    private final RabbitTemplate rabbitTemplate;

    public MailSenderService(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void send(MailDto mailDto) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.EMAIL_EXCHANGE, RabbitMQConfig.EMAIL_ROUTING_KEY, mailDto);
    }
}
