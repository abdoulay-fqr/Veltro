package com.veltro.booking.dto;

import com.veltro.booking.entity.CourseLevel;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UpdateCourseRequest {
    private String name;
    private String description;
    private LocalDateTime dateTime;
    private Integer durationMinutes;
    private Integer capacity;
    private CourseLevel level;
    private String room;
}
