package com.veltro.user.repository;

import com.veltro.user.entity.AccountStatus;
import com.veltro.user.entity.CoachProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CoachProfileRepository extends JpaRepository<CoachProfile, Long> {

    Optional<CoachProfile> findByUserId(Long userId);

    boolean existsByUserId(Long userId);

    // Admin: list all coaches with optional status filter
    Page<CoachProfile> findByStatus(AccountStatus status, Pageable pageable);
}