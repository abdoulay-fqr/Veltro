package com.veltro.subscription.event;

import com.veltro.subscription.entity.Plan;
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
    private Plan plan;
    private LocalDate endDate;
    private int daysRemaining;
}
