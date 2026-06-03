package com.veltro.shop.dto;

import com.veltro.shop.entity.Product;
import com.veltro.shop.entity.ProductCategory;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ProductResponse {
    private Long id;
    private String name;
    private String description;
    private ProductCategory category;
    private BigDecimal price;
    private int stockQuantity;
    private String imageUrl;
    private boolean active;
    private LocalDateTime createdAt;

    public static ProductResponse from(Product p) {
        ProductResponse r = new ProductResponse();
        r.setId(p.getId());
        r.setName(p.getName());
        r.setDescription(p.getDescription());
        r.setCategory(p.getCategory());
        r.setPrice(p.getPrice());
        r.setStockQuantity(p.getStockQuantity());
        r.setImageUrl(p.getImageUrl());
        r.setActive(p.isActive());
        r.setCreatedAt(p.getCreatedAt());
        return r;
    }
}
