package com.veltro.shop.dto;

import com.veltro.shop.entity.ProductCategory;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateProductRequest {
    @NotBlank(message = "name is required")
    private String name;
    private String description;
    @NotNull(message = "category is required")
    private ProductCategory category;
    @NotNull @DecimalMin(value = "0.01", message = "price must be positive")
    private BigDecimal price;
    @Min(value = 0, message = "stockQuantity cannot be negative")
    private int stockQuantity;
    private String imageUrl;
}
