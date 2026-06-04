package com.veltro.booking.controller;

import com.veltro.booking.dto.CourseResponse;
import com.veltro.booking.dto.CreateCourseRequest;
import com.veltro.booking.dto.UpdateCourseRequest;
import com.veltro.booking.entity.CourseLevel;
import com.veltro.booking.entity.CourseStatus;
import com.veltro.booking.exception.BusinessRuleException;
import com.veltro.booking.service.CourseCommandService;
import com.veltro.booking.service.CourseQueryService;
import com.veltro.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/courses")
@RequiredArgsConstructor
@Tag(name = "Courses", description = "Course management — create, list, update, cancel")
public class CourseController {

    private final CourseCommandService commandService;
    private final CourseQueryService queryService;

    @Operation(summary = "Create a course (COACH only)")
    @PostMapping
    public ResponseEntity<ApiResponse<CourseResponse>> create(
            @Valid @RequestBody CreateCourseRequest req,
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Role") String role) {

        requireCoach(role);
        // COACH: always use their own userId. ADMIN: may supply coachId in body.
        if (req.getCoachId() == null) {
            req.setCoachId(userId);
        }
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Course created", commandService.create(req)));
    }

    @Operation(summary = "List all courses with optional filters")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<CourseResponse>>> list(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(required = false) CourseLevel level,
            @RequestParam(required = false) Long coachId,
            @RequestParam(required = false) CourseStatus status,
            @PageableDefault(size = 20, sort = "dateTime") Pageable pageable) {

        return ResponseEntity.ok(ApiResponse.success(
                queryService.findAll(from, to, level, coachId, status, pageable)));
    }

    @Operation(summary = "Get course detail with enrolled count and available spots")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CourseResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(queryService.findById(id)));
    }

    @Operation(summary = "Update course (COACH owner only)")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CourseResponse>> update(
            @PathVariable Long id,
            @RequestBody UpdateCourseRequest req,
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Role") String role) {

        requireCoach(role);
        return ResponseEntity.ok(ApiResponse.success("Course updated", commandService.update(id, req, userId)));
    }

    @Operation(summary = "Cancel course — publishes CourseCancelled event and notifies booked members")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> cancel(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Role") String role) {

        requireCoach(role);
        commandService.cancel(id, userId);
        return ResponseEntity.ok(ApiResponse.success("Course cancelled"));
    }

    private void requireCoach(String role) {
        if (!"COACH".equals(role) && !"ADMIN".equals(role) && !"SUPER_ADMIN".equals(role)) {
            throw new BusinessRuleException("COACH access required");
        }
    }
}
