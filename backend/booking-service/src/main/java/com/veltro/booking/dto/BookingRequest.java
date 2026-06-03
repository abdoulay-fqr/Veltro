package com.veltro.booking.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BookingRequest {

    @NotNull(message = "courseId is required")
    private Long courseId;

    private String memberEmail;
}
