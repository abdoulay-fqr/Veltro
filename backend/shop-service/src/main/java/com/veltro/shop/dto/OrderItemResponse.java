package com.veltro.shop.dto;

import com.veltro.shop.entity.OrderItem;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderItemResponse {
    private Long id;
    private Long productId;
    private String productName;
    private int quantity;
    private BigDecimal unitPrice;
    private BigDecimal lineTotal;

    public static OrderItemResponse from(OrderItem i) {
        OrderItemResponse r = new OrderItemResponse();
        r.setId(i.getId());
        r.setProductId(i.getProductId());
        r.setProductName(i.getProductName());
        r.setQuantity(i.getQuantity());
        r.setUnitPrice(i.getUnitPrice());
        r.setLineTotal(i.getUnitPrice().multiply(java.math.BigDecimal.valueOf(i.getQuantity())));
        return r;
    }
}
