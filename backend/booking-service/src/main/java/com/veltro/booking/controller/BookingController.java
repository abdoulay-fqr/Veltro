package com.veltro.booking.controller;

import com.veltro.booking.dto.BookingRequest;
import com.veltro.booking.dto.BookingResponse;
import com.veltro.booking.entity.RegistrationStatus;
import com.veltro.booking.service.BookingCommandService;
import com.veltro.booking.service.BookingQueryService;
import com.veltro.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
@Tag(name = "Bookings", description = "Course reservation and cancellation")
public class BookingController {

    private final BookingCommandService commandService;
    private final BookingQueryService queryService;

    @Operation(summary = "Book a course (validates subscription + 7-day window, waitlists if full)")
    @PostMapping
    public ResponseEntity<ApiResponse<BookingResponse>> book(
            @Valid @RequestBody BookingRequest req,
            @RequestHeader("X-User-Id") Long memberId) {

        BookingResponse response = commandService.book(req, memberId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        response.getStatus().name().equals("BOOKED") ? "Booking confirmed" : "Added to waitlist",
                        response));
    }

    @Operation(summary = "Cancel a booking (must be > 2 hours before course)")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<BookingResponse>> cancel(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long memberId) {

        return ResponseEntity.ok(ApiResponse.success("Booking cancelled", commandService.cancel(id, memberId)));
    }

    @Operation(summary = "Get member's bookings (filter by status: BOOKED, WAITLISTED, CANCELLED)")
    @GetMapping("/member/{memberId}")
    public ResponseEntity<ApiResponse<List<BookingResponse>>> getMemberBookings(
            @PathVariable Long memberId,
            @RequestParam(required = false) RegistrationStatus status) {

        return ResponseEntity.ok(ApiResponse.success(queryService.getMemberBookings(memberId, status)));
    }

    @Operation(summary = "Get all registrations for a course (COACH/ADMIN only)")
    @GetMapping("/course/{courseId}")
    public ResponseEntity<ApiResponse<List<BookingResponse>>> getCourseRegistrations(
            @PathVariable Long courseId,
            @RequestHeader("X-User-Id") Long coachId,
            @RequestHeader("X-User-Role") String role) {

        return ResponseEntity.ok(ApiResponse.success(queryService.getCourseRegistrations(courseId, coachId)));
    }
}
