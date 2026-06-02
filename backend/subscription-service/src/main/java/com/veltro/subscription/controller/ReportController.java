package com.veltro.subscription.controller;

import com.veltro.common.dto.ApiResponse;
import com.veltro.subscription.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/subscriptions/reports")
@RequiredArgsConstructor
@Tag(name = "Reports", description = "Financial and subscription analytics")
public class ReportController {

    private final ReportService reportService;

    @Operation(summary = "Subscription summary: active count, revenue totals")
    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<Map<String, Object>>> summary() {
        return ResponseEntity.ok(ApiResponse.success(reportService.getSummary()));
    }

    @Operation(summary = "Monthly revenue breakdown")
    @GetMapping("/revenue")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> revenue() {
        return ResponseEntity.ok(ApiResponse.success(reportService.getMonthlyRevenue()));
    }

    @Operation(summary = "Active subscription plan distribution")
    @GetMapping("/distribution")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> distribution() {
        return ResponseEntity.ok(ApiResponse.success(reportService.getPlanDistribution()));
    }
}
