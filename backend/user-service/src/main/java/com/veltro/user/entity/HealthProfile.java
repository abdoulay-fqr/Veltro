package com.veltro.user.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "health_profile")
public class HealthProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "member_profile_id", nullable = false, unique = true)
    private MemberProfile memberProfile;

    @Column(name = "fitness_objective")
    private String fitnessObjective;

    @Column(name = "medical_restrictions", columnDefinition = "TEXT")
    private String medicalRestrictions;

    // DECIMAL(5,2) matches the SQL migration — e.g. 95.50 kg
    @Column(name = "weight_kg", precision = 5, scale = 2)
    private BigDecimal weightKg;

    // DECIMAL(5,2) matches the SQL migration — e.g. 175.00 cm
    @Column(name = "height_cm", precision = 5, scale = 2)
    private BigDecimal heightCm;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}