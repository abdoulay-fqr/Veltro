package com.veltro.booking.controller;

import com.veltro.booking.dto.AttendanceResponse;
import com.veltro.booking.dto.MarkAttendanceRequest;
import com.veltro.booking.exception.BusinessRuleException;
import com.veltro.booking.service.AttendanceService;
import com.veltro.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/attendance")
@RequiredArgsConstructor
@Tag(name = "Attendance", description = "Course attendance marking and retrieval")
public class AttendanceController {

    private final AttendanceService attendanceService;

    @Operation(summary = "Mark attendance for a course (COACH only, after course start time)")
    @PostMapping
    public ResponseEntity<ApiResponse<List<AttendanceResponse>>> mark(
            @Valid @RequestBody MarkAttendanceRequest req,
            @RequestHeader("X-User-Id") Long coachId,
            @RequestHeader("X-User-Role") String role) {

        if (!"COACH".equals(role) && !"ADMIN".equals(role) && !"SUPER_ADMIN".equals(role)) {
            throw new BusinessRuleException("COACH access required");
        }
        return ResponseEntity.ok(ApiResponse.success("Attendance recorded",
                attendanceService.markAttendance(req, coachId)));
    }

    @Operation(summary = "Get attendance records for a course")
    @GetMapping("/course/{courseId}")
    public ResponseEntity<ApiResponse<List<AttendanceResponse>>> getCourseAttendance(
            @PathVariable Long courseId) {

        return ResponseEntity.ok(ApiResponse.success(attendanceService.getCourseAttendance(courseId)));
    }
}
