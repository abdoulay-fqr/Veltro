package com.veltro.user.repository;

import com.veltro.user.entity.AdminProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdminProfileRepository extends JpaRepository<AdminProfile, Long> {

    Optional<AdminProfile> findByUserId(Long userId);

    boolean existsByUserId(Long userId);
}