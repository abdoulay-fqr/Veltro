package com.veltro.auth.repository;

import com.veltro.auth.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByIdentifier(String identifier);
    boolean existsByIdentifier(String identifier);
}