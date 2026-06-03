package com.veltro.shop.controller;

import com.veltro.common.dto.ApiResponse;
import com.veltro.shop.dto.OrderResponse;
import com.veltro.shop.dto.PlaceOrderRequest;
import com.veltro.shop.service.OrderCommandService;
import com.veltro.shop.service.OrderQueryService;
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

@RestController
@RequestMapping("/api/v1/shop/orders")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "Member order management")
public class OrderController {

    private final OrderCommandService commandService;
    private final OrderQueryService queryService;

    @Operation(summary = "Place an order (validate stock, decrement, snapshot)")
    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponse>> placeOrder(
            @Valid @RequestBody PlaceOrderRequest req,
            @RequestHeader("X-User-Id") Long memberId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Order placed", commandService.placeOrder(memberId, req)));
    }

    @Operation(summary = "Get current member's order history")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<OrderResponse>>> myOrders(
            @RequestHeader("X-User-Id") Long memberId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(queryService.getMemberOrders(memberId, pageable)));
    }

    @Operation(summary = "Get order detail")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(queryService.getOrderById(id)));
    }

    @Operation(summary = "Cancel a PENDING order (restores stock)")
    @PutMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<OrderResponse>> cancel(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long memberId) {
        return ResponseEntity.ok(ApiResponse.success("Order cancelled", commandService.cancelOrder(id, memberId)));
    }
}
