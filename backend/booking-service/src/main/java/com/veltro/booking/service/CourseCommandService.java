package com.veltro.booking.service;

import com.veltro.booking.config.RabbitMQConfig;
import com.veltro.booking.dto.CourseResponse;
import com.veltro.booking.dto.CreateCourseRequest;
import com.veltro.booking.dto.UpdateCourseRequest;
import com.veltro.booking.entity.Course;
import com.veltro.booking.entity.CourseStatus;
import com.veltro.booking.entity.RegistrationStatus;
import com.veltro.booking.event.CourseCancelledEvent;
import com.veltro.booking.exception.BusinessRuleException;
import com.veltro.booking.exception.ResourceNotFoundException;
import com.veltro.booking.repository.CourseRegistrationRepository;
import com.veltro.booking.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CourseCommandService {

    private final CourseRepository courseRepo;
    private final CourseRegistrationRepository registrationRepo;
    private final RabbitTemplate rabbitTemplate;
    private final Clock clock;

    @Transactional
    public CourseResponse create(CreateCourseRequest req) {
        Course course = new Course();
        course.setCoachId(req.getCoachId());
        course.setName(req.getName());
        course.setDescription(req.getDescription());
        course.setDateTime(req.getDateTime());
        course.setDurationMinutes(req.getDurationMinutes());
        course.setCapacity(req.getCapacity());
        course.setLevel(req.getLevel());
        course.setRoom(req.getRoom());
        return CourseResponse.from(courseRepo.save(course));
    }

    @Transactional
    public CourseResponse update(Long id, UpdateCourseRequest req, Long coachId) {
        Course course = getOwnCourseOrThrow(id, coachId);

        if (course.getStatus() == CourseStatus.CANCELLED) {
            throw new BusinessRuleException("Cannot update a cancelled course");
        }
        if (req.getName() != null)           course.setName(req.getName());
        if (req.getDescription() != null)    course.setDescription(req.getDescription());
        if (req.getDateTime() != null) {
            if (req.getDateTime().isBefore(LocalDateTime.now(clock))) {
                throw new BusinessRuleException("dateTime must be in the future");
            }
            course.setDateTime(req.getDateTime());
        }
        if (req.getDurationMinutes() != null) course.setDurationMinutes(req.getDurationMinutes());
        if (req.getCapacity() != null) {
            if (req.getCapacity() < course.getEnrolledCount()) {
                throw new BusinessRuleException("New capacity cannot be less than current enrolled count");
            }
            course.setCapacity(req.getCapacity());
        }
        if (req.getLevel() != null) course.setLevel(req.getLevel());
        if (req.getRoom() != null)  course.setRoom(req.getRoom());

        return CourseResponse.from(courseRepo.save(course));
    }

    @Transactional
    public void cancel(Long id, Long coachId) {
        Course course = getOwnCourseOrThrow(id, coachId);

        if (course.getStatus() == CourseStatus.CANCELLED) {
            throw new BusinessRuleException("Course is already cancelled");
        }

        // Collect affected BOOKED members before cancelling
        var bookedRegs = registrationRepo.findByCourseIdAndStatusIn(
                id, List.of(RegistrationStatus.BOOKED, RegistrationStatus.WAITLISTED));

        course.setStatus(CourseStatus.CANCELLED);
        courseRepo.save(course);

        List<Long> memberIds = bookedRegs.stream().map(r -> r.getMemberId()).toList();
        List<String> memberEmails = bookedRegs.stream()
                .filter(r -> r.getMemberEmail() != null)
                .map(r -> r.getMemberEmail()).toList();

        CourseCancelledEvent event = new CourseCancelledEvent(
                course.getId(), course.getName(), course.getDateTime(),
                course.getCoachId(), memberIds, memberEmails);

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.BOOKING_EXCHANGE,
                RabbitMQConfig.COURSE_CANCELLED_ROUTING_KEY,
                event);

        log.info("Course {} cancelled, notifying {} members", id, memberIds.size());
    }

    private Course getOwnCourseOrThrow(Long courseId, Long coachId) {
        Course course = courseRepo.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found: " + courseId));
        if (!course.getCoachId().equals(coachId)) {
            throw new BusinessRuleException("You can only modify your own courses");
        }
        return course;
    }
}
