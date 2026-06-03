package com.veltro.activity.dto;

import com.veltro.activity.entity.MachineType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class SessionRequest {

    @NotNull(message = "memberId is required")
    private Long memberId;

    private String memberEmail;

    @NotNull(message = "machineType is required")
    private MachineType machineType;

    @Positive(message = "durationMinutes must be positive")
    private int durationMinutes;

    private Integer caloriesBurned;
    private Double distanceKm;
    private Integer avgHeartRate;
}
