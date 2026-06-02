package com.veltro.subscription.entity;

import java.math.BigDecimal;

public enum Plan {
    TRIAL(7,   BigDecimal.ZERO),
    SESSION(1, new BigDecimal("15.00")),
    MONTHLY(30, new BigDecimal("39.00")),
    ANNUAL(365, new BigDecimal("374.00"));

    private final int durationDays;
    private final BigDecimal price;

    Plan(int durationDays, BigDecimal price) {
        this.durationDays = durationDays;
        this.price = price;
    }

    public int getDurationDays() { return durationDays; }
    public BigDecimal getPrice()  { return price; }
}
