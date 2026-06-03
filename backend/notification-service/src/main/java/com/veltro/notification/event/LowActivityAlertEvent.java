package com.veltro.notification.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LowActivityAlertEvent {
    private Long memberId;
    private String memberEmail;
    private int sessionCount;
    private LocalDate weekStart;
}
