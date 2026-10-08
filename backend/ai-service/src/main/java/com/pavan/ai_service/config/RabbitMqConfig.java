package com.pavan.ai_service.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    @Bean
    public DirectExchange documentExchange(
            @Value("${app.rabbitmq.document-exchange}") String exchangeName) {
        return new DirectExchange(exchangeName);
    }

    @Bean
    public Queue documentUploadedQueue(
            @Value("${app.rabbitmq.document-uploaded-queue}") String queueName) {
        return new Queue(queueName, true);
    }

    @Bean
    public Binding documentUploadedBinding(
            Queue documentUploadedQueue,
            DirectExchange documentExchange,
            @Value("${app.rabbitmq.document-uploaded-routing-key}") String routingKey) {
        return BindingBuilder.bind(documentUploadedQueue)
                .to(documentExchange)
                .with(routingKey);
    }

    @Bean
    public MessageConverter messageConverter() {
        return new JacksonJsonMessageConverter();
    }
}
