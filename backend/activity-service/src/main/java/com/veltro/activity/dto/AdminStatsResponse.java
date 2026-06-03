package com.veltro.activity.dto;

import lombok.Data;

import java.util.Map;

@Data
public class AdminStatsResponse {

    private Map<Integer, Long> entriesByHour;  // hour (0-23) -> count
    private int peakHour;
    private long todayEntries;
    private long weeklyEntries;
    private long currentOccupancy;
}
