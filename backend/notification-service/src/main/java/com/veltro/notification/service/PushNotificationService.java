package com.veltro.notification.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@Slf4j
public class PushNotificationService {

    public void sendSubscriptionExpiring(Long memberId, String plan, int daysRemaining) {
        log.info("[FCM-STUB] Push → memberId={}, msg='Your {} subscription expires in {} day(s)'",
                memberId, plan, daysRemaining);
    }

    public void sendCourseCancelled(Long memberId, String courseName) {
        log.info("[FCM-STUB] Push → memberId={}, msg='Your course \"{}\" has been cancelled'",
                memberId, courseName);
    }

    public void sendWaitlistPromoted(Long memberId, String courseName) {
        log.info("[FCM-STUB] Push → memberId={}, msg='Spot opened in \"{}\" — you are now BOOKED'",
                memberId, courseName);
    }

    public void sendMemberWarning(Long memberId, int warningLevel) {
        String msg = warningLevel == 2
                ? "Absence warning 2/3 — next absence suspends your account"
                : "Your account has been suspended due to 3 unjustified absences";
        log.info("[FCM-STUB] Push → memberId={}, level={}, msg='{}'", memberId, warningLevel, msg);
    }

    public void sendCourseReminder(Long memberId, String courseName, LocalDateTime dateTime) {
        String formatted = dateTime.format(DateTimeFormatter.ofPattern("HH:mm"));
        log.info("[FCM-STUB] Push → memberId={}, msg='Reminder: \"{}\" starts at {}'",
                memberId, courseName, formatted);
    }
}
