package com.veltro.user.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class HealthProfileRequest {
    private String fitnessObjective;
    private String medicalRestrictions;
    private BigDecimal weightKg;
    private BigDecimal heightCm;
}
