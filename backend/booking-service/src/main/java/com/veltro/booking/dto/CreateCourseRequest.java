package com.veltro.booking.dto;

import com.veltro.booking.entity.CourseLevel;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CreateCourseRequest {

    // Set from X-User-Id header, not from request body
    private Long coachId;

    @NotBlank(message = "name is required")
    private String name;

    private String description;

    @NotNull(message = "dateTime is required")
    @Future(message = "dateTime must be in the future")
    private LocalDateTime dateTime;

    @Positive(message = "durationMinutes must be positive")
    private int durationMinutes = 60;

    @Min(value = 1, message = "capacity must be at least 1")
    private int capacity;

    @NotNull(message = "level is required")
    private CourseLevel level;

    private String room;
}
