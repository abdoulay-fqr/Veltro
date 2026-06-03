package com.veltro.booking.service;

import com.veltro.booking.config.RabbitMQConfig;
import com.veltro.booking.entity.CourseStatus;
import com.veltro.booking.entity.RegistrationStatus;
import com.veltro.booking.event.CourseReminderEvent;
import com.veltro.booking.repository.CourseRegistrationRepository;
import com.veltro.booking.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReminderSchedulerService {

    private final CourseRepository courseRepo;
    private final CourseRegistrationRepository registrationRepo;
    private final RabbitTemplate rabbitTemplate;
    private final Clock clock;

    @Scheduled(fixedDelay = 15 * 60 * 1000) // every 15 minutes
    @Transactional
    public void sendCourseReminders() {
        LocalDateTime now = LocalDateTime.now(clock);
        LocalDateTime from = now.plusMinutes(90);   // 1.5h from now
        LocalDateTime to = now.plusMinutes(150);     // 2.5h from now

        var upcomingCourses = courseRepo.findByStatusAndDateTimeBetweenAndReminderSentFalse(
                CourseStatus.SCHEDULED, from, to);

        for (var course : upcomingCourses) {
            var bookedRegs = registrationRepo.findByCourseIdAndStatusIn(
                    course.getId(), List.of(RegistrationStatus.BOOKED));

            List<Long> memberIds = bookedRegs.stream().map(r -> r.getMemberId()).toList();
            List<String> memberEmails = bookedRegs.stream()
                    .filter(r -> r.getMemberEmail() != null)
                    .map(r -> r.getMemberEmail()).toList();

            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.BOOKING_EXCHANGE,
                    RabbitMQConfig.COURSE_REMINDER_ROUTING_KEY,
                    new CourseReminderEvent(
                            course.getId(), course.getName(), course.getDateTime(),
                            course.getRoom(), memberIds, memberEmails));

            course.setReminderSent(true);
            courseRepo.save(course);
            log.info("Course reminder sent for courseId={}, {} members", course.getId(), memberIds.size());
        }
    }
}
