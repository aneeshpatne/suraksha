package com.aneesh.suraksha.config;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    public static final String EMAIL_EXCHANGE = "email.exchange";
    public static final String EMAIL_QUEUE = "email.queue";
    public static final String EMAIL_ROUTING_KEY = "email.send";

    @Bean
    public TopicExchange emailExchange() {
        return new TopicExchange(EMAIL_EXCHANGE);
    }

    @Bean
    public org.springframework.amqp.support.converter.MessageConverter converter() {
        return new org.springframework.amqp.support.converter.Jackson2JsonMessageConverter();
    }

    @Bean
    public org.springframework.amqp.rabbit.core.RabbitTemplate amqpTemplate(
            org.springframework.amqp.rabbit.connection.ConnectionFactory connectionFactory) {
        final org.springframework.amqp.rabbit.core.RabbitTemplate rabbitTemplate = new org.springframework.amqp.rabbit.core.RabbitTemplate(
                connectionFactory);
        rabbitTemplate.setMessageConverter(converter());
        return rabbitTemplate;
    }

}
