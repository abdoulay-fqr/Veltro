package com.veltro.shop.controller;

import com.veltro.common.dto.ApiResponse;
import com.veltro.shop.dto.OrderResponse;
import com.veltro.shop.entity.OrderStatus;
import com.veltro.shop.exception.BusinessRuleException;
import com.veltro.shop.service.OrderCommandService;
import com.veltro.shop.service.OrderQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/shop/admin")
@RequiredArgsConstructor
@Tag(name = "Admin Shop", description = "Admin order management and revenue reporting")
public class AdminOrderController {

    private final OrderCommandService commandService;
    private final OrderQueryService queryService;

    @Operation(summary = "List all orders with optional status filter (ADMIN only)")
    @GetMapping("/orders")
    public ResponseEntity<ApiResponse<Page<OrderResponse>>> listAll(
            @RequestParam(required = false) OrderStatus status,
            @PageableDefault(size = 20) Pageable pageable,
            @RequestHeader("X-User-Role") String role) {
        requireAdmin(role);
        return ResponseEntity.ok(ApiResponse.success(queryService.getAllOrders(status, pageable)));
    }

    @Operation(summary = "Confirm a PENDING order (ADMIN only)")
    @PutMapping("/orders/{id}/confirm")
    public ResponseEntity<ApiResponse<OrderResponse>> confirm(
            @PathVariable Long id,
            @RequestHeader("X-User-Role") String role) {
        requireAdmin(role);
        return ResponseEntity.ok(ApiResponse.success("Order confirmed",
                commandService.updateStatus(id, OrderStatus.CONFIRMED)));
    }

    @Operation(summary = "Ship a CONFIRMED order (ADMIN only)")
    @PutMapping("/orders/{id}/ship")
    public ResponseEntity<ApiResponse<OrderResponse>> ship(
            @PathVariable Long id,
            @RequestHeader("X-User-Role") String role) {
        requireAdmin(role);
        return ResponseEntity.ok(ApiResponse.success("Order shipped",
                commandService.updateStatus(id, OrderStatus.SHIPPED)));
    }

    @Operation(summary = "Total shop revenue (CONFIRMED + SHIPPED + DELIVERED orders)")
    @GetMapping("/revenue")
    public ResponseEntity<ApiResponse<Map<String, BigDecimal>>> revenue(
            @RequestHeader("X-User-Role") String role) {
        requireAdmin(role);
        return ResponseEntity.ok(ApiResponse.success(Map.of("totalRevenue", queryService.getTotalRevenue())));
    }

    private void requireAdmin(String role) {
        if (!"ADMIN".equals(role) && !"SUPER_ADMIN".equals(role)) {
            throw new BusinessRuleException("ADMIN access required");
        }
    }
}
