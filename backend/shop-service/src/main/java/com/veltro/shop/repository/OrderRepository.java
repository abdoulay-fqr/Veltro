package com.veltro.shop.repository;

import com.veltro.shop.entity.OrderStatus;
import com.veltro.shop.entity.ShopOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<ShopOrder, Long> {

    Page<ShopOrder> findByMemberIdOrderByCreatedAtDesc(Long memberId, Pageable pageable);

    Page<ShopOrder> findByStatusOrderByCreatedAtDesc(OrderStatus status, Pageable pageable);

    Page<ShopOrder> findAllByOrderByCreatedAtDesc(Pageable pageable);

    List<ShopOrder> findByMemberIdAndStatus(Long memberId, OrderStatus status);

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM ShopOrder o WHERE o.status IN ('CONFIRMED','SHIPPED','DELIVERED')")
    BigDecimal sumRevenue();
}
