package com.veltro.shop.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class PlaceOrderRequest {

    @NotEmpty(message = "items cannot be empty")
    private List<OrderItemRequest> items;

    @NotNull(message = "shippingAddress is required")
    private String shippingAddress;
}
