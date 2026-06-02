package com.veltro.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${notification.email.from}")
    private String fromAddress;

    public void sendSubscriptionExpiringEmail(String to, String plan, int daysRemaining) {
        if (to == null || to.isBlank()) {
            log.warn("Cannot send subscription expiring email — recipient is blank");
            return;
        }
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(fromAddress);
            msg.setTo(to);
            msg.setSubject("Your Veltro " + plan + " subscription is expiring soon");
            msg.setText(
                    "Hi there,\n\n" +
                    "Your " + plan + " subscription expires in " + daysRemaining + " day(s).\n\n" +
                    "Log in to Veltro to renew your plan and keep your gym access uninterrupted.\n\n" +
                    "— The Veltro Team"
            );
            mailSender.send(msg);
            log.info("Subscription expiring email sent to={}, plan={}, daysRemaining={}", to, plan, daysRemaining);
        } catch (Exception e) {
            log.error("Failed to send subscription expiring email to={}: {}", to, e.getMessage());
            throw new RuntimeException("Email delivery failed", e);
        }
    }
}
