package com.veltro.booking.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class MarkAttendanceRequest {

    @NotNull(message = "courseId is required")
    private Long courseId;

    @NotEmpty(message = "attendance records are required")
    private List<AttendanceRecordDto> records;
}
