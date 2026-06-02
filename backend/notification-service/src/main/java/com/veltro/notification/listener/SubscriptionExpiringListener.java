package com.veltro.notification.listener;

import com.veltro.notification.config.RabbitMQConfig;
import com.veltro.notification.event.SubscriptionExpiringEvent;
import com.veltro.notification.service.EmailService;
import com.veltro.notification.service.PushNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SubscriptionExpiringListener {

    private final EmailService emailService;
    private final PushNotificationService pushService;

    @RabbitListener(queues = RabbitMQConfig.NOTIFICATION_SUB_EXPIRING_QUEUE)
    public void onSubscriptionExpiring(SubscriptionExpiringEvent event) {
        log.info("Processing SubscriptionExpiringEvent: subscriptionId={}, memberId={}, daysRemaining={}",
                event.getSubscriptionId(), event.getMemberId(), event.getDaysRemaining());

        try {
            // 1. Send push notification (FCM stub)
            pushService.sendSubscriptionExpiring(
                    event.getMemberId(), event.getPlan(), event.getDaysRemaining());

            // 2. Send email (via Mailtrap in dev)
            emailService.sendSubscriptionExpiringEmail(
                    event.getMemberEmail(), event.getPlan(), event.getDaysRemaining());

            log.info("Notification sent successfully for subscriptionId={}", event.getSubscriptionId());
        } catch (Exception e) {
            log.error("Notification attempt FAILED for subscriptionId={}: {}",
                    event.getSubscriptionId(), e.getMessage());
            throw e; // Let Spring retry with backoff, then DLQ after 3 attempts
        }
    }

    @RabbitListener(queues = RabbitMQConfig.NOTIFICATION_SUB_EXPIRING_DLQ)
    public void onDeadLetterSubscriptionExpiring(SubscriptionExpiringEvent event) {
        log.error("[DLQ] Subscription expiring notification permanently failed for subscriptionId={}, memberId={}",
                event.getSubscriptionId(), event.getMemberId());
        // In production: alert ops team, write to incident log, etc.
    }
}
