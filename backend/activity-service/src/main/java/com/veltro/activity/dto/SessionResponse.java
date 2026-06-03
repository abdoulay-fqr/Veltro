package com.veltro.activity.dto;

import com.veltro.activity.entity.MachineSession;
import com.veltro.activity.entity.MachineType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SessionResponse {

    private Long id;
    private Long memberId;
    private MachineType machineType;
    private int durationMinutes;
    private Integer caloriesBurned;
    private Double distanceKm;
    private Integer avgHeartRate;
    private LocalDateTime recordedAt;

    public static SessionResponse from(MachineSession s) {
        SessionResponse r = new SessionResponse();
        r.setId(s.getId());
        r.setMemberId(s.getMemberId());
        r.setMachineType(s.getMachineType());
        r.setDurationMinutes(s.getDurationMinutes());
        r.setCaloriesBurned(s.getCaloriesBurned());
        r.setDistanceKm(s.getDistanceKm());
        r.setAvgHeartRate(s.getAvgHeartRate());
        r.setRecordedAt(s.getRecordedAt());
        return r;
    }
}
