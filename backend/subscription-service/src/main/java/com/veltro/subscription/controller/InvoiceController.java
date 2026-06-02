package com.veltro.subscription.controller;

import com.veltro.common.dto.ApiResponse;
import com.veltro.subscription.dto.PaymentRecordResponse;
import com.veltro.subscription.service.SubscriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/subscriptions/{memberId}/invoices")
@RequiredArgsConstructor
@Tag(name = "Invoices", description = "Payment history and invoice management")
public class InvoiceController {

    private final SubscriptionService subscriptionService;

    @Operation(summary = "List all payment records for a member")
    @GetMapping
    public ResponseEntity<ApiResponse<List<PaymentRecordResponse>>> list(@PathVariable Long memberId) {
        return ResponseEntity.ok(ApiResponse.success(subscriptionService.getInvoicesByMemberId(memberId)));
    }

    @Operation(summary = "Get a single invoice by ID for a member")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PaymentRecordResponse>> getById(
            @PathVariable Long memberId,
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(subscriptionService.getInvoiceById(memberId, id)));
    }
}
