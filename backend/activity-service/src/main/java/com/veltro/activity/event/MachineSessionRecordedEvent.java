package com.veltro.activity.event;

import com.veltro.activity.entity.MachineType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MachineSessionRecordedEvent {
    private Long sessionId;
    private Long memberId;
    private MachineType machineType;
    private int durationMinutes;
    private Integer caloriesBurned;
    private LocalDateTime recordedAt;
}
