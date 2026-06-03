package com.veltro.shop.controller;

import com.veltro.common.dto.ApiResponse;
import com.veltro.shop.dto.CreateProductRequest;
import com.veltro.shop.dto.ProductResponse;
import com.veltro.shop.entity.ProductCategory;
import com.veltro.shop.exception.BusinessRuleException;
import com.veltro.shop.service.ProductCommandService;
import com.veltro.shop.service.ProductQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/shop/products")
@RequiredArgsConstructor
@Tag(name = "Products", description = "Product catalog management")
public class ProductController {

    private final ProductCommandService commandService;
    private final ProductQueryService queryService;

    @Operation(summary = "List active products, optionally filtered by category")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> list(
            @RequestParam(required = false) ProductCategory category,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(queryService.listActive(category, pageable)));
    }

    @Operation(summary = "Get product detail")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(queryService.getById(id)));
    }

    @Operation(summary = "Create product (ADMIN only)")
    @PostMapping
    public ResponseEntity<ApiResponse<ProductResponse>> create(
            @Valid @RequestBody CreateProductRequest req,
            @RequestHeader("X-User-Role") String role) {
        requireAdmin(role);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Product created", commandService.create(req)));
    }

    @Operation(summary = "Update product (ADMIN only)")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> update(
            @PathVariable Long id,
            @RequestBody CreateProductRequest req,
            @RequestHeader("X-User-Role") String role) {
        requireAdmin(role);
        return ResponseEntity.ok(ApiResponse.success("Product updated", commandService.update(id, req)));
    }

    @Operation(summary = "Soft delete product (ADMIN only)")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long id,
            @RequestHeader("X-User-Role") String role) {
        requireAdmin(role);
        commandService.softDelete(id);
        return ResponseEntity.ok(ApiResponse.success("Product deactivated"));
    }

    private void requireAdmin(String role) {
        if (!"ADMIN".equals(role) && !"SUPER_ADMIN".equals(role)) {
            throw new BusinessRuleException("ADMIN access required");
        }
    }
}
