package com.veltro.notification.listener;

import com.veltro.notification.event.LowActivityAlertEvent;
import com.veltro.notification.service.EmailService;
import com.veltro.notification.service.PushNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ActivityEventListener {

    private final PushNotificationService pushService;
    private final EmailService emailService;

    @RabbitListener(queues = "veltro.notification.low-activity-alert.queue")
    public void onLowActivityAlert(LowActivityAlertEvent event) {
        log.info("LowActivityAlert: memberId={}, sessions={}", event.getMemberId(), event.getSessionCount());
        try {
            pushService.sendLowActivityAlert(event.getMemberId(), event.getSessionCount());
            if (event.getMemberEmail() != null && !event.getMemberEmail().isBlank()) {
                emailService.sendLowActivityAlertEmail(
                        event.getMemberEmail(), event.getSessionCount(), event.getWeekStart());
            }
        } catch (Exception e) {
            log.error("Failed to process LowActivityAlert for memberId={}: {}", event.getMemberId(), e.getMessage());
            throw e;
        }
    }

    @RabbitListener(queues = "veltro.notification.low-activity-alert.queue.dlq")
    public void onDeadLetterLowActivityAlert(LowActivityAlertEvent event) {
        log.error("[DLQ] LowActivityAlert notification permanently failed for memberId={}", event.getMemberId());
    }
}
