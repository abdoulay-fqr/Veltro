package com.veltro.shop.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String SHOP_EXCHANGE            = "veltro.shop.exchange";
    public static final String ORDER_PLACED_ROUTING_KEY = "order.placed";

    public static final String NOTIFICATION_ORDER_PLACED_QUEUE = "veltro.notification.order-placed.queue";
    public static final String NOTIFICATION_DLX                = "veltro.notification.dlx";

    @Bean
    public TopicExchange shopExchange() {
        return new TopicExchange(SHOP_EXCHANGE, true, false);
    }

    @Bean
    public DirectExchange notificationDlx() {
        return new DirectExchange(NOTIFICATION_DLX, true, false);
    }

    @Bean
    public Queue notificationOrderPlacedQueue() {
        return QueueBuilder.durable(NOTIFICATION_ORDER_PLACED_QUEUE)
                .withArgument("x-dead-letter-exchange", NOTIFICATION_DLX)
                .withArgument("x-dead-letter-routing-key", NOTIFICATION_ORDER_PLACED_QUEUE + ".dlq")
                .build();
    }

    @Bean
    public Queue orderPlacedDlq() {
        return new Queue(NOTIFICATION_ORDER_PLACED_QUEUE + ".dlq", true);
    }

    @Bean
    public Binding orderPlacedBinding(Queue notificationOrderPlacedQueue, TopicExchange shopExchange) {
        return BindingBuilder.bind(notificationOrderPlacedQueue).to(shopExchange).with(ORDER_PLACED_ROUTING_KEY);
    }

    @Bean
    public Binding orderPlacedDlqBinding(Queue orderPlacedDlq, DirectExchange notificationDlx) {
        return BindingBuilder.bind(orderPlacedDlq).to(notificationDlx).with(NOTIFICATION_ORDER_PLACED_QUEUE + ".dlq");
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
