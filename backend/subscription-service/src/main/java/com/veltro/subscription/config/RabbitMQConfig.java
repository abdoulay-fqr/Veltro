package com.veltro.subscription.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // ── Consume from user-service ─────────────────────────────────────────
    public static final String USER_EXCHANGE              = "veltro.user.exchange";
    public static final String USER_CREATED_ROUTING_KEY   = "user.created";
    // Subscription-service declares its own queue so it gets its own copy (fan-out via topic)
    public static final String SUB_USER_CREATED_QUEUE     = "veltro.subscription.user-created.queue";

    // ── Publish to notification-service ──────────────────────────────────
    public static final String SUBSCRIPTION_EXCHANGE              = "veltro.subscription.exchange";
    public static final String SUBSCRIPTION_EXPIRING_ROUTING_KEY  = "subscription.expiring";
    public static final String NOTIFICATION_SUB_EXPIRING_QUEUE    = "veltro.notification.subscription-expiring.queue";
    public static final String NOTIFICATION_DLX                   = "veltro.notification.dlx";
    public static final String NOTIFICATION_SUB_EXPIRING_DLQ      = "veltro.notification.subscription-expiring.dlq";

    // ── Consume queue (UserCreated fan-out) ───────────────────────────────

    @Bean
    public TopicExchange userExchange() {
        return new TopicExchange(USER_EXCHANGE, true, false);
    }

    @Bean
    public Queue subUserCreatedQueue() {
        return new Queue(SUB_USER_CREATED_QUEUE, true);
    }

    @Bean
    public Binding subUserCreatedBinding(Queue subUserCreatedQueue, TopicExchange userExchange) {
        return BindingBuilder.bind(subUserCreatedQueue).to(userExchange).with(USER_CREATED_ROUTING_KEY);
    }

    // ── Publish exchange ─────────────────────────────────────────────────

    @Bean
    public TopicExchange subscriptionExchange() {
        return new TopicExchange(SUBSCRIPTION_EXCHANGE, true, false);
    }

    // ── Notification queues (declared here so messages persist even before notification-service starts)

    @Bean
    public DirectExchange notificationDlx() {
        return new DirectExchange(NOTIFICATION_DLX, true, false);
    }

    @Bean
    public Queue notificationSubExpiringQueue() {
        return QueueBuilder.durable(NOTIFICATION_SUB_EXPIRING_QUEUE)
                .withArgument("x-dead-letter-exchange", NOTIFICATION_DLX)
                .withArgument("x-dead-letter-routing-key", NOTIFICATION_SUB_EXPIRING_DLQ)
                .build();
    }

    @Bean
    public Queue notificationSubExpiringDlq() {
        return new Queue(NOTIFICATION_SUB_EXPIRING_DLQ, true);
    }

    @Bean
    public Binding notificationSubExpiringBinding(
            Queue notificationSubExpiringQueue, TopicExchange subscriptionExchange) {
        return BindingBuilder
                .bind(notificationSubExpiringQueue)
                .to(subscriptionExchange)
                .with(SUBSCRIPTION_EXPIRING_ROUTING_KEY);
    }

    @Bean
    public Binding notificationSubExpiringDlqBinding(
            Queue notificationSubExpiringDlq, DirectExchange notificationDlx) {
        return BindingBuilder
                .bind(notificationSubExpiringDlq)
                .to(notificationDlx)
                .with(NOTIFICATION_SUB_EXPIRING_DLQ);
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
        factory.setDefaultRequeueRejected(false);
        return factory;
    }
}
