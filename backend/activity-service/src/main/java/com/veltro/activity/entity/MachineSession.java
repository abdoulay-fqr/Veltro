package com.veltro.activity.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "machine_session")
public class MachineSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "member_email")
    private String memberEmail;

    @Enumerated(EnumType.STRING)
    @Column(name = "machine_type", nullable = false)
    private MachineType machineType;

    @Column(name = "duration_minutes", nullable = false)
    private int durationMinutes;

    @Column(name = "calories_burned")
    private Integer caloriesBurned;

    @Column(name = "distance_km")
    private Double distanceKm;

    @Column(name = "avg_heart_rate")
    private Integer avgHeartRate;

    @Column(name = "recorded_at", nullable = false, updatable = false)
    private LocalDateTime recordedAt;

    @PrePersist
    protected void onCreate() {
        if (recordedAt == null) {
            recordedAt = LocalDateTime.now();
        }
    }
}
