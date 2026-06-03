package com.veltro.shop.service;

import com.veltro.shop.dto.ProductResponse;
import com.veltro.shop.entity.ProductCategory;
import com.veltro.shop.exception.ResourceNotFoundException;
import com.veltro.shop.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductQueryService {

    private final ProductRepository productRepo;

    @Transactional(readOnly = true)
    public Page<ProductResponse> listActive(ProductCategory category, Pageable pageable) {
        if (category != null) {
            return productRepo.findByActiveTrueAndCategoryOrderByCreatedAtDesc(category, pageable)
                    .map(ProductResponse::from);
        }
        return productRepo.findByActiveTrueOrderByCreatedAtDesc(pageable).map(ProductResponse::from);
    }

    @Transactional(readOnly = true)
    public ProductResponse getById(Long id) {
        return ProductResponse.from(productRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + id)));
    }
}
