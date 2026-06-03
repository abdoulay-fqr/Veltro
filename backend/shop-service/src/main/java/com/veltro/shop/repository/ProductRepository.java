package com.veltro.shop.repository;

import com.veltro.shop.entity.Product;
import com.veltro.shop.entity.ProductCategory;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    Page<Product> findByActiveTrueAndCategoryOrderByCreatedAtDesc(ProductCategory category, Pageable pageable);

    Page<Product> findByActiveTrueOrderByCreatedAtDesc(Pageable pageable);

    @Lock(LockModeType.OPTIMISTIC)
    @Query("SELECT p FROM Product p WHERE p.id = :id AND p.active = true")
    Optional<Product> findByIdActiveForUpdate(@Param("id") Long id);
}
