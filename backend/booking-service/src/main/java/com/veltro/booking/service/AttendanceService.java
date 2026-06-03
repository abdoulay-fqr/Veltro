package com.veltro.booking.service;

import com.veltro.booking.client.UserClient;
import com.veltro.booking.config.RabbitMQConfig;
import com.veltro.booking.dto.AttendanceRecordDto;
import com.veltro.booking.dto.AttendanceResponse;
import com.veltro.booking.dto.MarkAttendanceRequest;
import com.veltro.booking.entity.Attendance;
import com.veltro.booking.entity.Course;
import com.veltro.booking.entity.CourseStatus;
import com.veltro.booking.event.MemberWarningEvent;
import com.veltro.booking.exception.BusinessRuleException;
import com.veltro.booking.exception.ResourceNotFoundException;
import com.veltro.booking.repository.AttendanceRepository;
import com.veltro.booking.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AttendanceService {

    private final AttendanceRepository attendanceRepo;
    private final CourseRepository courseRepo;
    private final UserClient userClient;
    private final RabbitTemplate rabbitTemplate;
    private final Clock clock;

    @Value("${attendance.absence-threshold:3}")
    private int absenceThreshold;

    @Transactional
    public List<AttendanceResponse> markAttendance(MarkAttendanceRequest req, Long coachId) {
        Course course = courseRepo.findById(req.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course not found: " + req.getCourseId()));

        if (!course.getCoachId().equals(coachId)) {
            throw new BusinessRuleException("You can only mark attendance for your own courses");
        }
        if (LocalDateTime.now(clock).isBefore(course.getDateTime())) {
            throw new BusinessRuleException("Attendance can only be marked after the course has started");
        }

        List<AttendanceResponse> results = new ArrayList<>();
        for (AttendanceRecordDto record : req.getRecords()) {
            Attendance att = new Attendance();
            att.setCourseId(req.getCourseId());
            att.setMemberId(record.getMemberId());
            att.setMemberEmail(record.getMemberEmail());
            att.setPresent(record.isPresent());
            att.setMarkedByCoachId(coachId);
            attendanceRepo.save(att);
            results.add(AttendanceResponse.from(att));

            if (!record.isPresent()) {
                processAbsence(record.getMemberId(), record.getMemberEmail(), course.getName());
            }
        }

        // Mark course as COMPLETED once attendance is recorded
        if (course.getStatus() == CourseStatus.SCHEDULED) {
            course.setStatus(CourseStatus.COMPLETED);
            courseRepo.save(course);
        }

        return results;
    }

    @Transactional(readOnly = true)
    public List<AttendanceResponse> getCourseAttendance(Long courseId) {
        return attendanceRepo.findByCourseId(courseId).stream()
                .map(AttendanceResponse::from).toList();
    }

    private void processAbsence(Long memberId, String memberEmail, String courseName) {
        long absenceCount = attendanceRepo.countByMemberIdAndPresentFalseAndProcessedForSuspensionFalse(memberId);
        int warningLevel = (int) Math.min(absenceCount, absenceThreshold);

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.BOOKING_EXCHANGE,
                RabbitMQConfig.MEMBER_WARNING_ROUTING_KEY,
                new MemberWarningEvent(memberId, memberEmail, warningLevel, absenceCount, courseName));

        log.info("Published MemberWarning level={} for memberId={}", warningLevel, memberId);

        if (absenceCount >= absenceThreshold) {
            suspendMember(memberId);
            // Mark absences as processed
            attendanceRepo.findByMemberIdAndPresentFalseAndProcessedForSuspensionFalse(memberId)
                    .forEach(a -> {
                        a.setProcessedForSuspension(true);
                        attendanceRepo.save(a);
                    });
        }
    }

    private void suspendMember(Long memberId) {
        try {
            userClient.suspendMember(memberId, "ADMIN");
            log.info("Member {} suspended after {} absences", memberId, absenceThreshold);
        } catch (Exception e) {
            log.error("Failed to suspend member {}: {}", memberId, e.getMessage());
        }
    }
}
