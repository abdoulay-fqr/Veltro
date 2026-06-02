package com.veltro.subscription.controller;

import com.veltro.common.dto.ApiResponse;
import com.veltro.subscription.dto.*;
import com.veltro.subscription.entity.Plan;
import com.veltro.subscription.service.SubscriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/v1/subscriptions")
@RequiredArgsConstructor
@Tag(name = "Subscriptions", description = "Subscription lifecycle management")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @Operation(summary = "Create and activate a subscription plan for a member")
    @PostMapping
    public ResponseEntity<ApiResponse<SubscriptionResponse>> create(
            @Valid @RequestBody CreateSubscriptionRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Subscription created", subscriptionService.create(req)));
    }

    @Operation(summary = "Get active/paused subscription for a member")
    @GetMapping("/{memberId}")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> getActive(@PathVariable Long memberId) {
        return ResponseEntity.ok(ApiResponse.success(subscriptionService.getActiveByMemberId(memberId)));
    }

    @Operation(summary = "Get all subscriptions (history) for a member")
    @GetMapping("/{memberId}/all")
    public ResponseEntity<ApiResponse<List<SubscriptionResponse>>> getAll(@PathVariable Long memberId) {
        return ResponseEntity.ok(ApiResponse.success(subscriptionService.getAllByMemberId(memberId)));
    }

    @Operation(summary = "List all subscriptions (admin)")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<SubscriptionResponse>>> findAll(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable,
            @RequestHeader(value = "X-User-Role", defaultValue = "") String role) {
        return ResponseEntity.ok(ApiResponse.success(subscriptionService.findAll(pageable)));
    }

    @Operation(summary = "Cancel a subscription")
    @PutMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> cancel(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Subscription cancelled", subscriptionService.cancel(id)));
    }

    @Operation(summary = "Pause a subscription (max 2 months/year)")
    @PutMapping("/{id}/pause")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> pause(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Subscription paused", subscriptionService.pause(id)));
    }

    @Operation(summary = "Resume a paused subscription")
    @PutMapping("/{id}/resume")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> resume(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Subscription resumed", subscriptionService.resume(id)));
    }

    @Operation(summary = "Toggle auto-renewal flag")
    @PutMapping("/{id}/auto-renew")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> toggleAutoRenew(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Auto-renew toggled", subscriptionService.toggleAutoRenew(id)));
    }

    @Operation(summary = "List all available plans with pricing")
    @GetMapping("/plans")
    public ResponseEntity<ApiResponse<List<PlanInfo>>> getPlans() {
        List<PlanInfo> plans = Arrays.stream(Plan.values()).map(p -> new PlanInfo(
                p, p.getDurationDays(), p.getPrice(), describePlan(p)
        )).toList();
        return ResponseEntity.ok(ApiResponse.success(plans));
    }

    private String describePlan(Plan p) {
        return switch (p) {
            case TRIAL   -> "7-day free trial for new members";
            case SESSION -> "Single-day access pass";
            case MONTHLY -> "Full monthly membership (30 days)";
            case ANNUAL  -> "Annual membership with 20% discount";
        };
    }
}
