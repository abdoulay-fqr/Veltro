package com.veltro.notification.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.interceptor.RetryInterceptorBuilder;

@Configuration
public class RabbitMQConfig {

    public static final String SUBSCRIPTION_EXCHANGE              = "veltro.subscription.exchange";
    public static final String SUBSCRIPTION_EXPIRING_ROUTING_KEY  = "subscription.expiring";
    public static final String NOTIFICATION_SUB_EXPIRING_QUEUE    = "veltro.notification.subscription-expiring.queue";
    public static final String NOTIFICATION_DLX                   = "veltro.notification.dlx";
    public static final String NOTIFICATION_SUB_EXPIRING_DLQ      = "veltro.notification.subscription-expiring.dlq";
    // DLQ declared here so it exists even when activity-service starts after notification-service
    public static final String NOTIFICATION_LOW_ACTIVITY_ALERT_DLQ =
            "veltro.notification.low-activity-alert.queue.dlq";

    @Value("${notification.retry.max-attempts:3}")
    private int maxAttempts;

    @Value("${notification.retry.backoff-delay:2000}")
    private long backoffDelay;

    @Bean
    public TopicExchange subscriptionExchange() {
        return new TopicExchange(SUBSCRIPTION_EXCHANGE, true, false);
    }

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
    public Binding dlqBinding(Queue notificationSubExpiringDlq, DirectExchange notificationDlx) {
        return BindingBuilder
                .bind(notificationSubExpiringDlq)
                .to(notificationDlx)
                .with(NOTIFICATION_SUB_EXPIRING_DLQ);
    }

    @Bean
    public Queue notificationLowActivityAlertDlq() {
        return new Queue(NOTIFICATION_LOW_ACTIVITY_ALERT_DLQ, true);
    }

    @Bean
    public Binding notificationLowActivityAlertDlqBinding(
            Queue notificationLowActivityAlertDlq, DirectExchange notificationDlx) {
        return BindingBuilder
                .bind(notificationLowActivityAlertDlq)
                .to(notificationDlx)
                .with(NOTIFICATION_LOW_ACTIVITY_ALERT_DLQ);
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
        // defaultRequeueRejected=false above ensures messages exhausting all retries
        // are rejected (not requeued) and routed to the DLQ via x-dead-letter-exchange.
        factory.setAdviceChain(
                RetryInterceptorBuilder.stateless()
                        .maxAttempts(maxAttempts)
                        .backOffOptions(backoffDelay, 2.0, 30_000)
                        .build()
        );
        return factory;
    }
}
