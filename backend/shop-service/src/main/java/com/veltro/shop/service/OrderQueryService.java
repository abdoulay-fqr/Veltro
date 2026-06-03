package com.veltro.shop.service;

import com.veltro.shop.dto.OrderResponse;
import com.veltro.shop.entity.OrderStatus;
import com.veltro.shop.exception.ResourceNotFoundException;
import com.veltro.shop.repository.OrderItemRepository;
import com.veltro.shop.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class OrderQueryService {

    private final OrderRepository orderRepo;
    private final OrderItemRepository itemRepo;

    @Transactional(readOnly = true)
    public Page<OrderResponse> getMemberOrders(Long memberId, Pageable pageable) {
        return orderRepo.findByMemberIdOrderByCreatedAtDesc(memberId, pageable)
                .map(o -> OrderResponse.from(o, itemRepo.findByOrderId(o.getId())));
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long orderId) {
        var order = orderRepo.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));
        return OrderResponse.from(order, itemRepo.findByOrderId(orderId));
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> getAllOrders(OrderStatus status, Pageable pageable) {
        if (status != null) {
            return orderRepo.findByStatusOrderByCreatedAtDesc(status, pageable)
                    .map(o -> OrderResponse.from(o, itemRepo.findByOrderId(o.getId())));
        }
        return orderRepo.findAllByOrderByCreatedAtDesc(pageable)
                .map(o -> OrderResponse.from(o, itemRepo.findByOrderId(o.getId())));
    }

    @Transactional(readOnly = true)
    public BigDecimal getTotalRevenue() {
        return orderRepo.sumRevenue();
    }
}
