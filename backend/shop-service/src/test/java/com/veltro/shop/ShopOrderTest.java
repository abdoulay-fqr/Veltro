package com.veltro.shop;

import com.veltro.shop.dto.*;
import com.veltro.shop.entity.OrderStatus;
import com.veltro.shop.entity.ProductCategory;
import com.veltro.shop.exception.ConflictException;
import com.veltro.shop.service.OrderCommandService;
import com.veltro.shop.service.OrderQueryService;
import com.veltro.shop.service.ProductCommandService;
import com.veltro.shop.service.ProductQueryService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

class ShopOrderTest extends BaseIntegrationTest {

    @Autowired private ProductCommandService productCmd;
    @Autowired private ProductQueryService productQuery;
    @Autowired private OrderCommandService orderCmd;
    @Autowired private OrderQueryService orderQuery;

    @MockitoBean private RabbitTemplate rabbitTemplate;

    private ProductResponse createProduct(String name, int stock, double price) {
        CreateProductRequest req = new CreateProductRequest();
        req.setName(name);
        req.setCategory(ProductCategory.SUPPLEMENT);
        req.setPrice(BigDecimal.valueOf(price));
        req.setStockQuantity(stock);
        return productCmd.create(req);
    }

    @Test
    void placeOrderDecrementsStock() {
        ProductResponse product = createProduct("Protein Shake", 10, 29.99);

        PlaceOrderRequest req = new PlaceOrderRequest();
        req.setShippingAddress("123 Main St");
        OrderItemRequest item = new OrderItemRequest();
        item.setProductId(product.getId());
        item.setQuantity(3);
        req.setItems(List.of(item));

        OrderResponse order = orderCmd.placeOrder(1001L, req);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(order.getItems()).hasSize(1);
        assertThat(order.getTotalAmount()).isEqualByComparingTo("89.97");

        ProductResponse updated = productQuery.getById(product.getId());
        assertThat(updated.getStockQuantity()).isEqualTo(7);
    }

    @Test
    void orderInsufficientStockThrowsConflict() {
        ProductResponse product = createProduct("Limited Gear", 1, 99.99);

        PlaceOrderRequest req = new PlaceOrderRequest();
        req.setShippingAddress("456 Oak Ave");
        OrderItemRequest item = new OrderItemRequest();
        item.setProductId(product.getId());
        item.setQuantity(5);
        req.setItems(List.of(item));

        assertThatThrownBy(() -> orderCmd.placeOrder(1002L, req))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Insufficient stock");
    }

    @Test
    void cancelOrderRestoresStock() {
        ProductResponse product = createProduct("Gym Bag", 5, 49.99);

        PlaceOrderRequest req = new PlaceOrderRequest();
        req.setShippingAddress("789 Pine Rd");
        OrderItemRequest item = new OrderItemRequest();
        item.setProductId(product.getId());
        item.setQuantity(2);
        req.setItems(List.of(item));

        OrderResponse order = orderCmd.placeOrder(1003L, req);
        assertThat(productQuery.getById(product.getId()).getStockQuantity()).isEqualTo(3);

        orderCmd.cancelOrder(order.getId(), 1003L);
        assertThat(productQuery.getById(product.getId()).getStockQuantity()).isEqualTo(5);
        assertThat(orderQuery.getOrderById(order.getId()).getStatus()).isEqualTo(OrderStatus.CANCELLED);
    }

    @Test
    void orderPlacedEventPublished() {
        ProductResponse product = createProduct("Event Test Product", 10, 15.00);
        PlaceOrderRequest req = new PlaceOrderRequest();
        req.setShippingAddress("321 Elm St");
        OrderItemRequest item = new OrderItemRequest();
        item.setProductId(product.getId());
        item.setQuantity(1);
        req.setItems(List.of(item));

        orderCmd.placeOrder(1004L, req);

        verify(rabbitTemplate).convertAndSend(
                eq("veltro.shop.exchange"),
                eq("order.placed"),
                any()
        );
    }

    @Test
    void adminUpdateOrderStatus() {
        ProductResponse product = createProduct("Admin Test", 10, 25.00);
        PlaceOrderRequest req = new PlaceOrderRequest();
        req.setShippingAddress("999 Admin Blvd");
        OrderItemRequest item = new OrderItemRequest();
        item.setProductId(product.getId());
        item.setQuantity(1);
        req.setItems(List.of(item));

        OrderResponse order = orderCmd.placeOrder(1005L, req);
        OrderResponse confirmed = orderCmd.updateStatus(order.getId(), OrderStatus.CONFIRMED);
        assertThat(confirmed.getStatus()).isEqualTo(OrderStatus.CONFIRMED);

        OrderResponse shipped = orderCmd.updateStatus(order.getId(), OrderStatus.SHIPPED);
        assertThat(shipped.getStatus()).isEqualTo(OrderStatus.SHIPPED);
    }
}
