package com.veltro.booking.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "course_registration",
       uniqueConstraints = @UniqueConstraint(columnNames = {"course_id", "member_id"}))
public class CourseRegistration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "course_id", nullable = false)
    private Long courseId;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "member_email")
    private String memberEmail;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RegistrationStatus status = RegistrationStatus.BOOKED;

    @Column(name = "waitlist_position")
    private Integer waitlistPosition;

    @Column(name = "registered_at", nullable = false, updatable = false)
    private LocalDateTime registeredAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "promoted_at")
    private LocalDateTime promotedAt;

    @Column(name = "cancellation_reason")
    private String cancellationReason;

    @PrePersist
    protected void onCreate() {
        registeredAt = LocalDateTime.now();
    }
}
