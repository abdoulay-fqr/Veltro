package com.veltro.user.repository;

import com.veltro.user.entity.AccountStatus;
import com.veltro.user.entity.MemberProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberProfileRepository extends JpaRepository<MemberProfile, Long> {

    Optional<MemberProfile> findByUserId(Long userId);

    boolean existsByUserId(Long userId);

    // Admin: list all members with optional status filter
    Page<MemberProfile> findByStatus(AccountStatus status, Pageable pageable);
}