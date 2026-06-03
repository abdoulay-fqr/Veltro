package com.veltro.messaging.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String MESSAGING_EXCHANGE           = "veltro.messaging.exchange";
    public static final String MESSAGE_RECEIVED_ROUTING_KEY = "message.received";

    public static final String NOTIFICATION_MESSAGE_RECEIVED_QUEUE = "veltro.notification.message-received.queue";
    public static final String NOTIFICATION_DLX                    = "veltro.notification.dlx";

    @Bean
    public TopicExchange messagingExchange() {
        return new TopicExchange(MESSAGING_EXCHANGE, true, false);
    }

    @Bean
    public DirectExchange notificationDlx() {
        return new DirectExchange(NOTIFICATION_DLX, true, false);
    }

    @Bean
    public Queue notificationMessageReceivedQueue() {
        return QueueBuilder.durable(NOTIFICATION_MESSAGE_RECEIVED_QUEUE)
                .withArgument("x-dead-letter-exchange", NOTIFICATION_DLX)
                .withArgument("x-dead-letter-routing-key", NOTIFICATION_MESSAGE_RECEIVED_QUEUE + ".dlq")
                .build();
    }

    @Bean
    public Queue messageReceivedDlq() {
        return new Queue(NOTIFICATION_MESSAGE_RECEIVED_QUEUE + ".dlq", true);
    }

    @Bean
    public Binding messageReceivedBinding(Queue notificationMessageReceivedQueue, TopicExchange messagingExchange) {
        return BindingBuilder.bind(notificationMessageReceivedQueue).to(messagingExchange).with(MESSAGE_RECEIVED_ROUTING_KEY);
    }

    @Bean
    public Binding messageReceivedDlqBinding(Queue messageReceivedDlq, DirectExchange notificationDlx) {
        return BindingBuilder.bind(messageReceivedDlq).to(notificationDlx).with(NOTIFICATION_MESSAGE_RECEIVED_QUEUE + ".dlq");
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory, Jackson2JsonMessageConverter converter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(converter);
        return factory;
    }
}
