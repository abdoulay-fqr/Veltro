package com.veltro.subscription.dto;

import com.veltro.subscription.entity.Plan;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class PlanInfo {
    private Plan plan;
    private int durationDays;
    private BigDecimal price;
    private String description;
}
