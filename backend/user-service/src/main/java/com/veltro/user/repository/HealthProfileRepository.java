package com.veltro.user.repository;

import com.veltro.user.entity.HealthProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HealthProfileRepository extends JpaRepository<HealthProfile, Long> {

    // Find by the owning member's profile id
    Optional<HealthProfile> findByMemberProfileId(Long memberProfileId);

    // Find by the owning member's userId (join through MemberProfile)
    Optional<HealthProfile> findByMemberProfileUserId(Long userId);
}