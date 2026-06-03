package com.veltro.booking.dto;

import com.veltro.booking.entity.CourseRegistration;
import com.veltro.booking.entity.RegistrationStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BookingResponse {

    private Long id;
    private Long courseId;
    private Long memberId;
    private String memberEmail;
    private RegistrationStatus status;
    private Integer waitlistPosition;
    private LocalDateTime registeredAt;
    private LocalDateTime cancelledAt;
    private LocalDateTime promotedAt;
    private String cancellationReason;

    public static BookingResponse from(CourseRegistration r) {
        BookingResponse b = new BookingResponse();
        b.setId(r.getId());
        b.setCourseId(r.getCourseId());
        b.setMemberId(r.getMemberId());
        b.setMemberEmail(r.getMemberEmail());
        b.setStatus(r.getStatus());
        b.setWaitlistPosition(r.getWaitlistPosition());
        b.setRegisteredAt(r.getRegisteredAt());
        b.setCancelledAt(r.getCancelledAt());
        b.setPromotedAt(r.getPromotedAt());
        b.setCancellationReason(r.getCancellationReason());
        return b;
    }
}
