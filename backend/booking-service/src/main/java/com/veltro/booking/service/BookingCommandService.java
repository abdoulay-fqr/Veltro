package com.veltro.booking.service;

import com.veltro.booking.client.SubscriptionClient;
import com.veltro.booking.config.RabbitMQConfig;
import com.veltro.booking.dto.BookingRequest;
import com.veltro.booking.dto.BookingResponse;
import com.veltro.booking.entity.Course;
import com.veltro.booking.entity.CourseRegistration;
import com.veltro.booking.entity.CourseStatus;
import com.veltro.booking.entity.RegistrationStatus;
import com.veltro.booking.event.WaitlistPromotedEvent;
import com.veltro.booking.exception.BusinessRuleException;
import com.veltro.booking.exception.ResourceNotFoundException;
import com.veltro.booking.repository.CourseRegistrationRepository;
import com.veltro.booking.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingCommandService {

    private final CourseRepository courseRepo;
    private final CourseRegistrationRepository registrationRepo;
    private final SubscriptionClient subscriptionClient;
    private final RabbitTemplate rabbitTemplate;
    private final Clock clock;

    @Value("${booking.window-days:7}")
    private int bookingWindowDays;

    @Value("${booking.cancellation-hours:2}")
    private int cancellationHours;

    @Transactional
    public BookingResponse book(BookingRequest req, Long memberId) {
        LocalDateTime now = LocalDateTime.now(clock);

        // 1. Validate subscription is ACTIVE
        validateActiveSubscription(memberId);

        // 2. Validate 7-day booking window
        Course course = courseRepo.findByIdForUpdate(req.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course not found: " + req.getCourseId()));

        if (course.getStatus() != CourseStatus.SCHEDULED) {
            throw new BusinessRuleException("Cannot book a course that is not scheduled");
        }

        long daysUntilCourse = ChronoUnit.DAYS.between(now, course.getDateTime());
        if (daysUntilCourse < 0 || daysUntilCourse > bookingWindowDays) {
            throw new BusinessRuleException(
                    "Booking is only allowed up to " + bookingWindowDays + " days in advance");
        }

        // 3. Prevent double booking
        boolean alreadyBooked = registrationRepo.existsByCourseIdAndMemberIdAndStatusNot(
                req.getCourseId(), memberId, RegistrationStatus.CANCELLED);
        if (alreadyBooked) {
            throw new BusinessRuleException("You already have a booking or waitlist spot for this course");
        }

        // 4. Book or waitlist
        CourseRegistration reg = new CourseRegistration();
        reg.setCourseId(req.getCourseId());
        reg.setMemberId(memberId);
        reg.setMemberEmail(req.getMemberEmail());

        if (course.getEnrolledCount() < course.getCapacity()) {
            reg.setStatus(RegistrationStatus.BOOKED);
            course.setEnrolledCount(course.getEnrolledCount() + 1);
            courseRepo.save(course);
        } else {
            int position = registrationRepo.countWaitlisted(req.getCourseId()) + 1;
            reg.setStatus(RegistrationStatus.WAITLISTED);
            reg.setWaitlistPosition(position);
        }

        return BookingResponse.from(registrationRepo.save(reg));
    }

    @Transactional
    public BookingResponse cancel(Long bookingId, Long memberId) {
        CourseRegistration reg = registrationRepo.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found: " + bookingId));

        if (!reg.getMemberId().equals(memberId)) {
            throw new BusinessRuleException("You can only cancel your own bookings");
        }
        if (reg.getStatus() == RegistrationStatus.CANCELLED) {
            throw new BusinessRuleException("Booking is already cancelled");
        }

        Course course = courseRepo.findById(reg.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));

        // Validate 2-hour cancellation window
        long hoursUntilCourse = ChronoUnit.HOURS.between(LocalDateTime.now(clock), course.getDateTime());
        if (hoursUntilCourse < cancellationHours) {
            throw new BusinessRuleException(
                    "Cancellations must be made at least " + cancellationHours + " hours before the course");
        }

        boolean wasBooked = reg.getStatus() == RegistrationStatus.BOOKED;
        int formerWaitlistPos = reg.getWaitlistPosition() != null ? reg.getWaitlistPosition() : 0;

        reg.setStatus(RegistrationStatus.CANCELLED);
        reg.setCancelledAt(LocalDateTime.now(clock));
        registrationRepo.save(reg);

        if (wasBooked) {
            course.setEnrolledCount(Math.max(0, course.getEnrolledCount() - 1));
            courseRepo.save(course);

            // Promote first waitlisted member
            registrationRepo.findFirstByCourseIdAndStatusOrderByWaitlistPositionAsc(
                    course.getId(), RegistrationStatus.WAITLISTED).ifPresent(waitlisted -> {
                waitlisted.setStatus(RegistrationStatus.BOOKED);
                waitlisted.setWaitlistPosition(null);
                waitlisted.setPromotedAt(LocalDateTime.now(clock));
                registrationRepo.save(waitlisted);

                course.setEnrolledCount(course.getEnrolledCount() + 1);
                courseRepo.save(course);

                // Shift remaining waitlist positions down
                registrationRepo.shiftWaitlistPositionsDown(course.getId(), 1);

                // Publish WaitlistPromoted event
                rabbitTemplate.convertAndSend(
                        RabbitMQConfig.BOOKING_EXCHANGE,
                        RabbitMQConfig.WAITLIST_PROMOTED_ROUTING_KEY,
                        new WaitlistPromotedEvent(
                                course.getId(), course.getName(), course.getDateTime(),
                                waitlisted.getMemberId(), waitlisted.getMemberEmail()));

                log.info("Promoted waitlisted member {} to BOOKED for course {}", waitlisted.getMemberId(), course.getId());
            });
        } else {
            // Was WAITLISTED: shift positions of higher-positioned members down
            registrationRepo.shiftWaitlistPositionsDown(course.getId(), formerWaitlistPos);
        }

        return BookingResponse.from(reg);
    }

    private void validateActiveSubscription(Long memberId) {
        try {
            var response = subscriptionClient.getActiveSubscription(memberId);
            if (response == null || response.getData() == null) {
                throw new BusinessRuleException("No active subscription found. Please subscribe first.");
            }
            Object status = response.getData().get("status");
            if (!"ACTIVE".equals(status)) {
                throw new BusinessRuleException(
                        "Your subscription is " + status + ". Please renew before booking.");
            }
        } catch (BusinessRuleException e) {
            throw e;
        } catch (Exception e) {
            // ServiceUnavailableException from fallback propagates as-is
            throw e;
        }
    }
}
