package com.veltro.notification.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderPlacedEvent {
    private Long orderId;
    private Long memberId;
    private BigDecimal totalAmount;
    private String shippingAddress;
    private List<String> itemSummaries;
    private LocalDateTime placedAt;
}
