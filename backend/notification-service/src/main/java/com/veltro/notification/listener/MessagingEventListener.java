package com.veltro.notification.listener;

import com.veltro.notification.event.MessageReceivedEvent;
import com.veltro.notification.event.OrderPlacedEvent;
import com.veltro.notification.service.EmailService;
import com.veltro.notification.service.PushNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class MessagingEventListener {

    private final PushNotificationService pushService;
    private final EmailService emailService;

    @RabbitListener(queues = "veltro.notification.message-received.queue")
    public void onMessageReceived(MessageReceivedEvent event) {
        log.info("MessageReceived: messageId={}, recipientId={}", event.getMessageId(), event.getRecipientId());
        try {
            // Push only — no email for chat messages
            pushService.sendNewMessage(
                    event.getRecipientId(),
                    event.getSenderName(),
                    event.getContentPreview());
        } catch (Exception e) {
            log.error("Failed to process MessageReceivedEvent for messageId={}: {}", event.getMessageId(), e.getMessage());
            throw e;
        }
    }

    @RabbitListener(queues = "veltro.notification.message-received.queue.dlq")
    public void onDeadLetterMessageReceived(MessageReceivedEvent event) {
        log.error("[DLQ] MessageReceived notification permanently failed for messageId={}", event.getMessageId());
    }

    @RabbitListener(queues = "veltro.notification.order-placed.queue")
    public void onOrderPlaced(OrderPlacedEvent event) {
        log.info("OrderPlaced: orderId={}, memberId={}", event.getOrderId(), event.getMemberId());
        try {
            pushService.sendOrderPlaced(event.getMemberId(), event.getOrderId());
        } catch (Exception e) {
            log.error("Failed to process OrderPlacedEvent for orderId={}: {}", event.getOrderId(), e.getMessage());
            throw e;
        }
    }

    @RabbitListener(queues = "veltro.notification.order-placed.queue.dlq")
    public void onDeadLetterOrderPlaced(OrderPlacedEvent event) {
        log.error("[DLQ] OrderPlaced notification permanently failed for orderId={}", event.getOrderId());
    }
}
