package com.veltro.shop.dto;

import com.veltro.shop.entity.OrderItem;
import com.veltro.shop.entity.OrderStatus;
import com.veltro.shop.entity.ShopOrder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderResponse {
    private Long id;
    private Long memberId;
    private OrderStatus status;
    private BigDecimal totalAmount;
    private String shippingAddress;
    private List<OrderItemResponse> items;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static OrderResponse from(ShopOrder o, List<OrderItem> items) {
        OrderResponse r = new OrderResponse();
        r.setId(o.getId());
        r.setMemberId(o.getMemberId());
        r.setStatus(o.getStatus());
        r.setTotalAmount(o.getTotalAmount());
        r.setShippingAddress(o.getShippingAddress());
        r.setItems(items.stream().map(OrderItemResponse::from).toList());
        r.setCreatedAt(o.getCreatedAt());
        r.setUpdatedAt(o.getUpdatedAt());
        return r;
    }
}
