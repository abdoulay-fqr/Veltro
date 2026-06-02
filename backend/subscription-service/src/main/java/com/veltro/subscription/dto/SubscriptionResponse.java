package com.veltro.subscription.dto;

import com.veltro.subscription.entity.Plan;
import com.veltro.subscription.entity.Subscription;
import com.veltro.subscription.entity.SubscriptionStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Data
public class SubscriptionResponse {

    private Long id;
    private Long memberId;
    private String memberEmail;
    private Plan plan;
    private BigDecimal planPrice;
    private SubscriptionStatus status;
    private LocalDate startDate;
    private LocalDate endDate;
    private boolean autoRenew;
    private int pausedMonthsUsed;
    private int daysRemaining;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static SubscriptionResponse from(Subscription s) {
        SubscriptionResponse r = new SubscriptionResponse();
        r.setId(s.getId());
        r.setMemberId(s.getMemberId());
        r.setMemberEmail(s.getMemberEmail());
        r.setPlan(s.getPlan());
        r.setPlanPrice(s.getPlan().getPrice());
        r.setStatus(s.getStatus());
        r.setStartDate(s.getStartDate());
        r.setEndDate(s.getEndDate());
        r.setAutoRenew(s.isAutoRenew());
        r.setPausedMonthsUsed(s.getPausedMonthsUsed());
        r.setDaysRemaining((int) Math.max(0, ChronoUnit.DAYS.between(LocalDate.now(), s.getEndDate())));
        r.setCreatedAt(s.getCreatedAt());
        r.setUpdatedAt(s.getUpdatedAt());
        return r;
    }
}
