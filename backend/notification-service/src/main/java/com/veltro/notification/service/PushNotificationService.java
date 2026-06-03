package com.veltro.notification.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * FCM HTTP v1 push notification service.
 * Attempts real FCM delivery if configured; falls back to console stub.
 * Uses device token per member — in production this would be fetched from a member→token registry.
 */
@Service
@Slf4j
public class PushNotificationService {

    @Value("${fcm.project-id:}")
    private String fcmProjectId;

    @Value("${fcm.server-key:}")
    private String fcmServerKey;

    private final WebClient webClient = WebClient.builder().build();

    private void sendPush(Long memberId, String title, String body) {
        // In production: fetch device token from a member→FCM token registry
        // For now: use member ID as a placeholder token marker
        String tokenPlaceholder = "member_" + memberId + "_device_token";

        if (!fcmProjectId.isBlank() && !fcmServerKey.isBlank()) {
            sendViaFcm(fcmProjectId, fcmServerKey, tokenPlaceholder, title, body);
        } else {
            log.info("[FCM-STUB] Push → memberId={}, title='{}', body='{}'", memberId, title, body);
        }
    }

    private void sendViaFcm(String projectId, String serverKey, String token, String title, String body) {
        try {
            Map<String, Object> message = Map.of(
                "message", Map.of(
                    "token", token,
                    "notification", Map.of("title", title, "body", body)
                )
            );

            webClient.post()
                    .uri("https://fcm.googleapis.com/v1/projects/" + projectId + "/messages:send")
                    .header("Authorization", "Bearer " + serverKey)
                    .header("Content-Type", "application/json")
                    .bodyValue(message)
                    .retrieve()
                    .bodyToMono(String.class)
                    .subscribe(
                            resp -> log.info("[FCM] Push delivered to memberId, token={}", token),
                            err -> log.warn("[FCM] Push delivery failed — graceful fallback: {}", err.getMessage())
                    );
        } catch (Exception e) {
            log.warn("[FCM] Push attempt failed — graceful fallback. memberId token={}: {}", token, e.getMessage());
        }
    }

    public void sendSubscriptionExpiring(Long memberId, String plan, int daysRemaining) {
        sendPush(memberId,
                "Subscription Expiring",
                "Your " + plan + " subscription expires in " + daysRemaining + " day(s)");
    }

    public void sendCourseCancelled(Long memberId, String courseName) {
        sendPush(memberId, "Course Cancelled",
                "Your course \"" + courseName + "\" has been cancelled");
    }

    public void sendWaitlistPromoted(Long memberId, String courseName) {
        sendPush(memberId, "You're In!",
                "A spot opened in \"" + courseName + "\" — you are now BOOKED");
    }

    public void sendMemberWarning(Long memberId, int warningLevel) {
        String body = warningLevel == 2
                ? "Absence warning 2/3 — next absence suspends your account"
                : "Your account has been suspended due to 3 unjustified absences";
        sendPush(memberId, "Absence Warning", body);
    }

    public void sendCourseReminder(Long memberId, String courseName, LocalDateTime dateTime) {
        String formatted = dateTime.format(DateTimeFormatter.ofPattern("HH:mm"));
        sendPush(memberId, "Class Reminder", "\"" + courseName + "\" starts at " + formatted);
    }

    public void sendLowActivityAlert(Long memberId, int sessionCount) {
        sendPush(memberId, "Keep It Up! 💪",
                "You've only had " + sessionCount + " session(s) this week. Head to the gym!");
    }

    public void sendNewMessage(Long recipientId, String senderName, String contentPreview) {
        sendPush(recipientId, "New message from " + senderName, contentPreview);
    }

    public void sendOrderPlaced(Long memberId, Long orderId) {
        sendPush(memberId, "Order confirmed! 🛍️",
                "Your order #" + orderId + " has been placed and is being processed.");
    }
}
