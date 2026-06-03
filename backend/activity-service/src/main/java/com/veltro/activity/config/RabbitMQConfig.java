package com.veltro.activity.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String ACTIVITY_EXCHANGE                  = "veltro.activity.exchange";
    public static final String GYM_ENTRY_RECORDED_ROUTING_KEY     = "activity.entry.recorded";
    public static final String MACHINE_SESSION_RECORDED_ROUTING_KEY = "activity.session.recorded";
    public static final String LOW_ACTIVITY_ALERT_ROUTING_KEY     = "activity.low-activity.alert";

    // Notification queues declared here so messages persist before notification-service starts
    public static final String NOTIFICATION_LOW_ACTIVITY_QUEUE = "veltro.notification.low-activity-alert.queue";
    public static final String NOTIFICATION_DLX                = "veltro.notification.dlx";

    @Bean
    public TopicExchange activityExchange() {
        return new TopicExchange(ACTIVITY_EXCHANGE, true, false);
    }

    @Bean
    public DirectExchange notificationDlx() {
        return new DirectExchange(NOTIFICATION_DLX, true, false);
    }

    @Bean
    public Queue notificationLowActivityQueue() {
        return QueueBuilder.durable(NOTIFICATION_LOW_ACTIVITY_QUEUE)
                .withArgument("x-dead-letter-exchange", NOTIFICATION_DLX)
                .withArgument("x-dead-letter-routing-key", NOTIFICATION_LOW_ACTIVITY_QUEUE + ".dlq")
                .build();
    }

    @Bean
    public Queue lowActivityDlq() {
        return new Queue(NOTIFICATION_LOW_ACTIVITY_QUEUE + ".dlq", true);
    }

    @Bean
    public Binding lowActivityBinding(Queue notificationLowActivityQueue, TopicExchange activityExchange) {
        return BindingBuilder.bind(notificationLowActivityQueue).to(activityExchange).with(LOW_ACTIVITY_ALERT_ROUTING_KEY);
    }

    @Bean
    public Binding lowActivityDlqBinding(Queue lowActivityDlq, DirectExchange notificationDlx) {
        return BindingBuilder.bind(lowActivityDlq).to(notificationDlx).with(NOTIFICATION_LOW_ACTIVITY_QUEUE + ".dlq");
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    @ConditionalOnBean(ConnectionFactory.class)
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory, Jackson2JsonMessageConverter converter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(converter);
        return factory;
    }
}
