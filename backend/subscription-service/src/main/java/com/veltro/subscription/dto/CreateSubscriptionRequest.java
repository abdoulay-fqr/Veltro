package com.veltro.subscription.dto;

import com.veltro.subscription.entity.PaymentMethod;
import com.veltro.subscription.entity.Plan;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateSubscriptionRequest {

    @NotNull(message = "memberId is required")
    private Long memberId;

    private String memberEmail;

    @NotNull(message = "plan is required")
    private Plan plan;

    private PaymentMethod paymentMethod = PaymentMethod.CARD;

    private boolean autoRenew = false;
}
