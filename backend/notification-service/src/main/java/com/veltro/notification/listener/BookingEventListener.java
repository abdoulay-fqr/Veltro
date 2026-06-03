package com.veltro.notification.listener;

import com.veltro.notification.event.*;
import com.veltro.notification.service.EmailService;
import com.veltro.notification.service.PushNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class BookingEventListener {

    private final EmailService emailService;
    private final PushNotificationService pushService;

    @RabbitListener(queues = "veltro.notification.course-cancelled.queue")
    public void onCourseCancelled(CourseCancelledEvent event) {
        log.info("CourseCancelled: courseId={}, affectedMembers={}", event.getCourseId(), event.getAffectedMemberIds().size());
        try {
            for (String email : event.getAffectedMemberEmails()) {
                emailService.sendCourseCancelledEmail(email, event.getCourseName(), event.getCourseDateTime());
            }
            event.getAffectedMemberIds().forEach(id ->
                    pushService.sendCourseCancelled(id, event.getCourseName()));
        } catch (Exception e) {
            log.error("Failed to process CourseCancelled for courseId={}: {}", event.getCourseId(), e.getMessage());
            throw e;
        }
    }

    @RabbitListener(queues = "veltro.notification.waitlist-promoted.queue")
    public void onWaitlistPromoted(WaitlistPromotedEvent event) {
        log.info("WaitlistPromoted: courseId={}, memberId={}", event.getCourseId(), event.getMemberId());
        try {
            emailService.sendWaitlistPromotedEmail(event.getMemberEmail(), event.getCourseName(), event.getCourseDateTime());
            pushService.sendWaitlistPromoted(event.getMemberId(), event.getCourseName());
        } catch (Exception e) {
            log.error("Failed to process WaitlistPromoted for memberId={}: {}", event.getMemberId(), e.getMessage());
            throw e;
        }
    }

    @RabbitListener(queues = "veltro.notification.member-warning.queue")
    public void onMemberWarning(MemberWarningEvent event) {
        log.info("MemberWarning: memberId={}, level={}", event.getMemberId(), event.getWarningLevel());
        try {
            emailService.sendMemberWarningEmail(event.getMemberEmail(), event.getWarningLevel(),
                    event.getAbsenceCount(), event.getCourseName());
            if (event.getWarningLevel() >= 2) {
                pushService.sendMemberWarning(event.getMemberId(), event.getWarningLevel());
            }
        } catch (Exception e) {
            log.error("Failed to process MemberWarning for memberId={}: {}", event.getMemberId(), e.getMessage());
            throw e;
        }
    }

    @RabbitListener(queues = "veltro.notification.course-reminder.queue")
    public void onCourseReminder(CourseReminderEvent event) {
        log.info("CourseReminder: courseId={}, members={}", event.getCourseId(), event.getBookedMemberIds().size());
        try {
            for (String email : event.getBookedMemberEmails()) {
                emailService.sendCourseReminderEmail(email, event.getCourseName(), event.getCourseDateTime(), event.getRoom());
            }
            event.getBookedMemberIds().forEach(id ->
                    pushService.sendCourseReminder(id, event.getCourseName(), event.getCourseDateTime()));
        } catch (Exception e) {
            log.error("Failed to process CourseReminder for courseId={}: {}", event.getCourseId(), e.getMessage());
            throw e;
        }
    }
}
