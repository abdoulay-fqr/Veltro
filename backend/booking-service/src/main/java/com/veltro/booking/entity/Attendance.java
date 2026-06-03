package com.veltro.booking.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "attendance",
       uniqueConstraints = @UniqueConstraint(columnNames = {"course_id", "member_id"}))
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "course_id", nullable = false)
    private Long courseId;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "member_email")
    private String memberEmail;

    @Column(nullable = false)
    private boolean present;

    @Column(name = "marked_at")
    private LocalDateTime markedAt;

    @Column(name = "marked_by_coach_id")
    private Long markedByCoachId;

    @Column(name = "processed_for_suspension", nullable = false)
    private boolean processedForSuspension = false;

    @PrePersist
    protected void onCreate() {
        markedAt = LocalDateTime.now();
    }
}
