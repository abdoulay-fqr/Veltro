package com.veltro.notification.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionExpiringEvent {
    private Long subscriptionId;
    private Long memberId;
    private String memberEmail;
    private String plan;
    private LocalDate endDate;
    private int daysRemaining;
}
