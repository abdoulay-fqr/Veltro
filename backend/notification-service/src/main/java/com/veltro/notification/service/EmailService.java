package com.veltro.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${notification.email.from}")
    private String fromAddress;

    private void send(String to, String subject, String body) {
        if (to == null || to.isBlank()) {
            log.warn("Cannot send email — recipient is blank. Subject: {}", subject);
            return;
        }
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(fromAddress);
            msg.setTo(to);
            msg.setSubject(subject);
            msg.setText(body);
            mailSender.send(msg);
            log.info("Email sent to={}, subject={}", to, subject);
        } catch (Exception e) {
            log.error("Failed to send email to={}: {}", to, e.getMessage());
            throw new RuntimeException("Email delivery failed", e);
        }
    }

    public void sendSubscriptionExpiringEmail(String to, String plan, int daysRemaining) {
        send(to,
            "Your Veltro " + plan + " subscription is expiring soon",
            "Hi there,\n\nYour " + plan + " subscription expires in " + daysRemaining + " day(s).\n\n"
            + "Log in to renew it.\n\n— The Veltro Team");
    }

    public void sendCourseCancelledEmail(String to, String courseName, LocalDateTime dateTime) {
        String formatted = dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        send(to,
            "Your course \"" + courseName + "\" has been cancelled",
            "Hi there,\n\nWe're sorry to inform you that the course \"" + courseName
            + "\" scheduled for " + formatted + " has been cancelled by the coach.\n\n"
            + "Please check the app for alternative sessions.\n\n— The Veltro Team");
    }

    public void sendWaitlistPromotedEmail(String to, String courseName, LocalDateTime dateTime) {
        String formatted = dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        send(to,
            "Great news! A spot opened in \"" + courseName + "\"",
            "Hi there,\n\nGood news! A spot has opened in \"" + courseName + "\" on " + formatted + ".\n\n"
            + "You've been automatically moved from the waitlist to BOOKED.\n\n— The Veltro Team");
    }

    public void sendMemberWarningEmail(String to, int level, long absenceCount, String courseName) {
        String subject = switch (level) {
            case 1 -> "Absence warning 1/3 — " + courseName;
            case 2 -> "Absence warning 2/3 — next absence suspends your account";
            default -> "Your account has been suspended due to 3 unjustified absences";
        };
        String body = switch (level) {
            case 1 -> "You have missed 1 session (" + courseName + "). Please attend your next class.";
            case 2 -> "You have missed 2 sessions. One more absence will result in account suspension.";
            default -> "Your account has been suspended due to 3 unjustified absences. Contact support.";
        };
        send(to, subject, body + "\n\n— The Veltro Team");
    }

    public void sendCourseReminderEmail(String to, String courseName, LocalDateTime dateTime, String room) {
        String formatted = dateTime.format(DateTimeFormatter.ofPattern("HH:mm"));
        send(to,
            "Reminder: \"" + courseName + "\" starts at " + formatted,
            "Hi there,\n\nThis is a reminder that \"" + courseName + "\" starts in about 2 hours at " + formatted
            + (room != null ? " in " + room : "") + ".\n\nSee you there!\n\n— The Veltro Team");
    }
}
