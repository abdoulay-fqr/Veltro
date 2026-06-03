package com.veltro.shop.service;

import com.veltro.shop.dto.CreateProductRequest;
import com.veltro.shop.dto.ProductResponse;
import com.veltro.shop.entity.Product;
import com.veltro.shop.exception.ResourceNotFoundException;
import com.veltro.shop.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductCommandService {

    private final ProductRepository productRepo;

    @Transactional
    public ProductResponse create(CreateProductRequest req) {
        Product p = new Product();
        p.setName(req.getName());
        p.setDescription(req.getDescription());
        p.setCategory(req.getCategory());
        p.setPrice(req.getPrice());
        p.setStockQuantity(req.getStockQuantity());
        p.setImageUrl(req.getImageUrl());
        return ProductResponse.from(productRepo.save(p));
    }

    @Transactional
    public ProductResponse update(Long id, CreateProductRequest req) {
        Product p = productRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + id));
        if (req.getName() != null)         p.setName(req.getName());
        if (req.getDescription() != null)  p.setDescription(req.getDescription());
        if (req.getCategory() != null)     p.setCategory(req.getCategory());
        if (req.getPrice() != null)        p.setPrice(req.getPrice());
        if (req.getStockQuantity() >= 0)   p.setStockQuantity(req.getStockQuantity());
        if (req.getImageUrl() != null)     p.setImageUrl(req.getImageUrl());
        return ProductResponse.from(productRepo.save(p));
    }

    @Transactional
    public void softDelete(Long id) {
        Product p = productRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + id));
        p.setActive(false);
        productRepo.save(p);
    }
}
