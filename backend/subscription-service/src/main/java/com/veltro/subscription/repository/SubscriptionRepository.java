package com.veltro.subscription.repository;

import com.veltro.subscription.entity.Plan;
import com.veltro.subscription.entity.Subscription;
import com.veltro.subscription.entity.SubscriptionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    Optional<Subscription> findFirstByMemberIdAndStatusInOrderByCreatedAtDesc(
            Long memberId, List<SubscriptionStatus> statuses);

    Optional<Subscription> findFirstByMemberIdOrderByCreatedAtDesc(Long memberId);

    List<Subscription> findByMemberIdOrderByCreatedAtDesc(Long memberId);

    boolean existsByMemberIdAndStatusIn(Long memberId, List<SubscriptionStatus> statuses);

    List<Subscription> findByStatusAndEndDateBefore(SubscriptionStatus status, LocalDate date);

    List<Subscription> findByStatusAndEndDateBetween(SubscriptionStatus status, LocalDate from, LocalDate to);

    Page<Subscription> findAll(Pageable pageable);

    long countByStatus(SubscriptionStatus status);

    long countByPlanAndStatus(Plan plan, SubscriptionStatus status);

    @Query(value = "SELECT s.plan, COUNT(*) FROM subscription s WHERE s.status = 'ACTIVE' GROUP BY s.plan",
           nativeQuery = true)
    List<Object[]> countByPlanForActive();
}
