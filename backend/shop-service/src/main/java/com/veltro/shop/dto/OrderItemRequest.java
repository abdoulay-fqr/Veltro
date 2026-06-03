package com.veltro.shop.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OrderItemRequest {
    @NotNull(message = "productId is required")
    private Long productId;
    @Min(value = 1, message = "quantity must be at least 1")
    private int quantity;
}
