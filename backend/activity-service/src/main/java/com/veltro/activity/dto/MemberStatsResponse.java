package com.veltro.activity.dto;

import lombok.Data;

@Data
public class MemberStatsResponse {

    private Long memberId;
    private long weeklyCalories;
    private long totalSessions;
    private Double avgHeartRate;
    private int currentStreak;

    // Personal records
    private int maxCaloriesInSession;
    private double maxDistanceKm;
    private int maxDurationMinutes;
}
