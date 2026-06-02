package com.veltro.user.dto;

import com.veltro.user.entity.HealthProfile;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class HealthProfileResponse {

    private Long id;
    private Long memberProfileId;
    private String fitnessObjective;
    private String medicalRestrictions;
    private BigDecimal weightKg;
    private BigDecimal heightCm;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static HealthProfileResponse from(HealthProfile h) {
        HealthProfileResponse r = new HealthProfileResponse();
        r.setId(h.getId());
        r.setMemberProfileId(h.getMemberProfile().getId());
        r.setFitnessObjective(h.getFitnessObjective());
        r.setMedicalRestrictions(h.getMedicalRestrictions());
        r.setWeightKg(h.getWeightKg());
        r.setHeightCm(h.getHeightCm());
        r.setCreatedAt(h.getCreatedAt());
        r.setUpdatedAt(h.getUpdatedAt());
        return r;
    }
}
