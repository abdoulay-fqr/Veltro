package com.veltro.user.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String USER_EXCHANGE                  = "veltro.user.exchange";
    public static final String USER_CREATED_ROUTING_KEY       = "user.created";
    public static final String USER_CREATED_QUEUE             = "veltro.user.created.queue";
    public static final String NFC_CARD_ACTIVATED_ROUTING_KEY = "nfc.card.activated";
    public static final String NFC_CARD_ACTIVATED_QUEUE       = "veltro.nfc.card.activated.queue";

    @Bean
    public TopicExchange userExchange() {
        return new TopicExchange(USER_EXCHANGE, true, false);
    }

    @Bean
    public Queue userCreatedQueue() {
        return new Queue(USER_CREATED_QUEUE, true);
    }

    @Bean
    public Queue nfcCardActivatedQueue() {
        return new Queue(NFC_CARD_ACTIVATED_QUEUE, true);
    }

    @Bean
    public Binding userCreatedBinding(Queue userCreatedQueue, TopicExchange userExchange) {
        return BindingBuilder
                .bind(userCreatedQueue)
                .to(userExchange)
                .with(USER_CREATED_ROUTING_KEY);
    }

    @Bean
    public Binding nfcCardActivatedBinding(Queue nfcCardActivatedQueue, TopicExchange userExchange) {
        return BindingBuilder
                .bind(nfcCardActivatedQueue)
                .to(userExchange)
                .with(NFC_CARD_ACTIVATED_ROUTING_KEY);
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}