package com.veltro.shop.service;

import com.veltro.shop.config.RabbitMQConfig;
import com.veltro.shop.dto.*;
import com.veltro.shop.entity.*;
import com.veltro.shop.event.OrderPlacedEvent;
import com.veltro.shop.exception.BusinessRuleException;
import com.veltro.shop.exception.ConflictException;
import com.veltro.shop.exception.ResourceNotFoundException;
import com.veltro.shop.repository.OrderItemRepository;
import com.veltro.shop.repository.OrderRepository;
import com.veltro.shop.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderCommandService {

    private final OrderRepository orderRepo;
    private final OrderItemRepository itemRepo;
    private final ProductRepository productRepo;
    private final RabbitTemplate rabbitTemplate;

    @CacheEvict(value = "productById", allEntries = true)
    @Transactional
    public OrderResponse placeOrder(Long memberId, PlaceOrderRequest req) {
        List<OrderItem> items = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        // Validate and decrement stock for all items atomically
        for (OrderItemRequest itemReq : req.getItems()) {
            Product product = productRepo.findByIdActiveForUpdate(itemReq.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Product not found or unavailable: " + itemReq.getProductId()));

            if (product.getStockQuantity() < itemReq.getQuantity()) {
                throw new ConflictException(
                        "Insufficient stock for product: " + product.getName() +
                        " (available: " + product.getStockQuantity() + ")");
            }

            product.setStockQuantity(product.getStockQuantity() - itemReq.getQuantity());
            productRepo.save(product);

            OrderItem item = new OrderItem();
            item.setProductId(product.getId());
            item.setProductName(product.getName());
            item.setQuantity(itemReq.getQuantity());
            item.setUnitPrice(product.getPrice());
            items.add(item);

            total = total.add(product.getPrice().multiply(BigDecimal.valueOf(itemReq.getQuantity())));
        }

        ShopOrder order = new ShopOrder();
        order.setMemberId(memberId);
        order.setTotalAmount(total);
        order.setShippingAddress(req.getShippingAddress());
        orderRepo.save(order);

        for (OrderItem item : items) {
            item.setOrderId(order.getId());
            itemRepo.save(item);
        }

        // Publish OrderPlaced event
        List<String> summaries = items.stream()
                .map(i -> i.getQuantity() + "x " + i.getProductName())
                .toList();
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.SHOP_EXCHANGE,
                RabbitMQConfig.ORDER_PLACED_ROUTING_KEY,
                new OrderPlacedEvent(order.getId(), memberId, total,
                        req.getShippingAddress(), summaries, LocalDateTime.now()));

        log.info("Order {} placed by memberId={}, total={}", order.getId(), memberId, total);
        return OrderResponse.from(order, items);
    }

    @CacheEvict(value = "productById", allEntries = true)
    @Transactional
    public OrderResponse cancelOrder(Long orderId, Long memberId) {
        ShopOrder order = orderRepo.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));

        if (!order.getMemberId().equals(memberId)) {
            throw new BusinessRuleException("You can only cancel your own orders");
        }
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new BusinessRuleException("Only PENDING orders can be cancelled");
        }

        // Restore stock
        List<OrderItem> items = itemRepo.findByOrderId(orderId);
        for (OrderItem item : items) {
            productRepo.findById(item.getProductId()).ifPresent(p -> {
                p.setStockQuantity(p.getStockQuantity() + item.getQuantity());
                productRepo.save(p);
            });
        }

        order.setStatus(OrderStatus.CANCELLED);
        orderRepo.save(order);
        return OrderResponse.from(order, items);
    }

    @Transactional
    public OrderResponse updateStatus(Long orderId, OrderStatus newStatus) {
        ShopOrder order = orderRepo.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));
        order.setStatus(newStatus);
        orderRepo.save(order);
        return OrderResponse.from(order, itemRepo.findByOrderId(orderId));
    }
}
