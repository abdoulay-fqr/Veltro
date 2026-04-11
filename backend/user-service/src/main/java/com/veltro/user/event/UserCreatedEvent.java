package com.veltro.user.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Published to RabbitMQ when a new MEMBER or COACH is created.
 * Consumed by subscription-service (Day 23) to auto-assign a TRIAL plan.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserCreatedEvent {

    private Long userId;      // app_user.id from auth-service
    private String role;      // "MEMBER" or "COACH"
    private String identifier; // email
}