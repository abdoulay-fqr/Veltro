package com.veltro.booking.service;

import com.veltro.booking.dto.BookingResponse;
import com.veltro.booking.entity.RegistrationStatus;
import com.veltro.booking.exception.BusinessRuleException;
import com.veltro.booking.repository.CourseRegistrationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingQueryService {

    private final CourseRegistrationRepository registrationRepo;

    @Transactional(readOnly = true)
    public List<BookingResponse> getMemberBookings(Long memberId, RegistrationStatus status) {
        List<RegistrationStatus> statuses = status != null
                ? List.of(status)
                : List.of(RegistrationStatus.BOOKED, RegistrationStatus.WAITLISTED, RegistrationStatus.CANCELLED);
        return registrationRepo
                .findByMemberIdAndStatusInOrderByRegisteredAtDesc(memberId, statuses)
                .stream().map(BookingResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> getCourseRegistrations(Long courseId, Long coachId) {
        return registrationRepo
                .findByCourseIdAndStatusIn(courseId,
                        List.of(RegistrationStatus.BOOKED, RegistrationStatus.WAITLISTED))
                .stream().map(BookingResponse::from).toList();
    }
}
