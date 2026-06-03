package com.veltro.booking.dto;

import com.veltro.booking.entity.Course;
import com.veltro.booking.entity.CourseLevel;
import com.veltro.booking.entity.CourseStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CourseResponse {

    private Long id;
    private Long coachId;
    private String name;
    private String description;
    private LocalDateTime dateTime;
    private int durationMinutes;
    private int capacity;
    private int enrolledCount;
    private int availableSpots;
    private double fillRate;
    private CourseLevel level;
    private String room;
    private CourseStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static CourseResponse from(Course c) {
        CourseResponse r = new CourseResponse();
        r.setId(c.getId());
        r.setCoachId(c.getCoachId());
        r.setName(c.getName());
        r.setDescription(c.getDescription());
        r.setDateTime(c.getDateTime());
        r.setDurationMinutes(c.getDurationMinutes());
        r.setCapacity(c.getCapacity());
        r.setEnrolledCount(c.getEnrolledCount());
        r.setAvailableSpots(c.getAvailableSpots());
        r.setFillRate(c.getCapacity() > 0 ? (double) c.getEnrolledCount() / c.getCapacity() : 0.0);
        r.setLevel(c.getLevel());
        r.setRoom(c.getRoom());
        r.setStatus(c.getStatus());
        r.setCreatedAt(c.getCreatedAt());
        r.setUpdatedAt(c.getUpdatedAt());
        return r;
    }
}
