package com.veltro.booking.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // ── Booking exchange (publish domain events) ──────────────────────────
    public static final String BOOKING_EXCHANGE               = "veltro.booking.exchange";
    public static final String COURSE_CANCELLED_ROUTING_KEY   = "course.cancelled";
    public static final String WAITLIST_PROMOTED_ROUTING_KEY  = "waitlist.promoted";
    public static final String MEMBER_WARNING_ROUTING_KEY     = "member.warning";
    public static final String COURSE_REMINDER_ROUTING_KEY    = "course.reminder";

    // ── Notification queues (declared here so events persist before notification-service starts)
    public static final String NOTIFICATION_COURSE_CANCELLED_QUEUE   = "veltro.notification.course-cancelled.queue";
    public static final String NOTIFICATION_WAITLIST_PROMOTED_QUEUE  = "veltro.notification.waitlist-promoted.queue";
    public static final String NOTIFICATION_MEMBER_WARNING_QUEUE     = "veltro.notification.member-warning.queue";
    public static final String NOTIFICATION_COURSE_REMINDER_QUEUE    = "veltro.notification.course-reminder.queue";
    public static final String NOTIFICATION_DLX                      = "veltro.notification.dlx";

    @Bean
    public TopicExchange bookingExchange() {
        return new TopicExchange(BOOKING_EXCHANGE, true, false);
    }

    @Bean
    public DirectExchange notificationDlx() {
        return new DirectExchange(NOTIFICATION_DLX, true, false);
    }

    private Queue durableQueueWithDlx(String name) {
        return QueueBuilder.durable(name)
                .withArgument("x-dead-letter-exchange", NOTIFICATION_DLX)
                .withArgument("x-dead-letter-routing-key", name + ".dlq")
                .build();
    }

    @Bean public Queue notificationCourseCancelledQueue()  { return durableQueueWithDlx(NOTIFICATION_COURSE_CANCELLED_QUEUE); }
    @Bean public Queue notificationWaitlistPromotedQueue() { return durableQueueWithDlx(NOTIFICATION_WAITLIST_PROMOTED_QUEUE); }
    @Bean public Queue notificationMemberWarningQueue()    { return durableQueueWithDlx(NOTIFICATION_MEMBER_WARNING_QUEUE); }
    @Bean public Queue notificationCourseReminderQueue()   { return durableQueueWithDlx(NOTIFICATION_COURSE_REMINDER_QUEUE); }

    @Bean public Queue courseCancelledDlq()  { return new Queue(NOTIFICATION_COURSE_CANCELLED_QUEUE + ".dlq", true); }
    @Bean public Queue waitlistPromotedDlq() { return new Queue(NOTIFICATION_WAITLIST_PROMOTED_QUEUE + ".dlq", true); }
    @Bean public Queue memberWarningDlq()    { return new Queue(NOTIFICATION_MEMBER_WARNING_QUEUE + ".dlq", true); }
    @Bean public Queue courseReminderDlq()   { return new Queue(NOTIFICATION_COURSE_REMINDER_QUEUE + ".dlq", true); }

    @Bean
    public Binding courseCancelledBinding(Queue notificationCourseCancelledQueue, TopicExchange bookingExchange) {
        return BindingBuilder.bind(notificationCourseCancelledQueue).to(bookingExchange).with(COURSE_CANCELLED_ROUTING_KEY);
    }
    @Bean
    public Binding waitlistPromotedBinding(Queue notificationWaitlistPromotedQueue, TopicExchange bookingExchange) {
        return BindingBuilder.bind(notificationWaitlistPromotedQueue).to(bookingExchange).with(WAITLIST_PROMOTED_ROUTING_KEY);
    }
    @Bean
    public Binding memberWarningBinding(Queue notificationMemberWarningQueue, TopicExchange bookingExchange) {
        return BindingBuilder.bind(notificationMemberWarningQueue).to(bookingExchange).with(MEMBER_WARNING_ROUTING_KEY);
    }
    @Bean
    public Binding courseReminderBinding(Queue notificationCourseReminderQueue, TopicExchange bookingExchange) {
        return BindingBuilder.bind(notificationCourseReminderQueue).to(bookingExchange).with(COURSE_REMINDER_ROUTING_KEY);
    }

    @Bean
    public Binding courseCancelledDlqBinding(Queue courseCancelledDlq, DirectExchange notificationDlx) {
        return BindingBuilder.bind(courseCancelledDlq).to(notificationDlx).with(NOTIFICATION_COURSE_CANCELLED_QUEUE + ".dlq");
    }
    @Bean
    public Binding waitlistPromotedDlqBinding(Queue waitlistPromotedDlq, DirectExchange notificationDlx) {
        return BindingBuilder.bind(waitlistPromotedDlq).to(notificationDlx).with(NOTIFICATION_WAITLIST_PROMOTED_QUEUE + ".dlq");
    }
    @Bean
    public Binding memberWarningDlqBinding(Queue memberWarningDlq, DirectExchange notificationDlx) {
        return BindingBuilder.bind(memberWarningDlq).to(notificationDlx).with(NOTIFICATION_MEMBER_WARNING_QUEUE + ".dlq");
    }
    @Bean
    public Binding courseReminderDlqBinding(Queue courseReminderDlq, DirectExchange notificationDlx) {
        return BindingBuilder.bind(courseReminderDlq).to(notificationDlx).with(NOTIFICATION_COURSE_REMINDER_QUEUE + ".dlq");
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
