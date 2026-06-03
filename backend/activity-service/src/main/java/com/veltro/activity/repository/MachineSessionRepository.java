package com.veltro.activity.repository;

import com.veltro.activity.entity.MachineSession;
import com.veltro.activity.entity.MachineType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MachineSessionRepository extends JpaRepository<MachineSession, Long> {

    Page<MachineSession> findByMemberIdOrderByRecordedAtDesc(Long memberId, Pageable pageable);

    List<MachineSession> findByMemberIdAndRecordedAtBetweenOrderByRecordedAtDesc(
            Long memberId, LocalDateTime from, LocalDateTime to);

    long countByMemberId(Long memberId);

    @Query(value = "SELECT COALESCE(SUM(calories_burned), 0) FROM machine_session " +
                   "WHERE member_id = :memberId AND YEARWEEK(recorded_at, 1) = YEARWEEK(NOW(), 1)",
           nativeQuery = true)
    Long sumWeeklyCaloriesForMember(@Param("memberId") Long memberId);

    // Last N sessions for avg heart rate
    @Query(value = "SELECT AVG(avg_heart_rate) FROM (" +
                   "  SELECT avg_heart_rate FROM machine_session WHERE member_id = :memberId AND avg_heart_rate IS NOT NULL " +
                   "  ORDER BY recorded_at DESC LIMIT :limit" +
                   ") AS sub",
           nativeQuery = true)
    Double avgHeartRateLastNSessions(@Param("memberId") Long memberId, @Param("limit") int limit);

    @Query(value = "SELECT COALESCE(MAX(calories_burned), 0) FROM machine_session WHERE member_id = :memberId",
           nativeQuery = true)
    Integer maxCaloriesForMember(@Param("memberId") Long memberId);

    @Query(value = "SELECT COALESCE(MAX(distance_km), 0) FROM machine_session WHERE member_id = :memberId",
           nativeQuery = true)
    Double maxDistanceForMember(@Param("memberId") Long memberId);

    @Query(value = "SELECT COALESCE(MAX(duration_minutes), 0) FROM machine_session WHERE member_id = :memberId",
           nativeQuery = true)
    Integer maxDurationForMember(@Param("memberId") Long memberId);

    // Weekly calories per day for chart (Mon-Sun)
    @Query(value = "SELECT DAYOFWEEK(recorded_at) AS day, COALESCE(SUM(calories_burned), 0) AS calories " +
                   "FROM machine_session WHERE member_id = :memberId AND YEARWEEK(recorded_at, 1) = YEARWEEK(NOW(), 1) " +
                   "GROUP BY DAYOFWEEK(recorded_at) ORDER BY DAYOFWEEK(recorded_at)",
           nativeQuery = true)
    List<Object[]> weeklyCaloriesByDayForMember(@Param("memberId") Long memberId);

    // Session count for last N weeks
    @Query(value = "SELECT YEARWEEK(recorded_at, 1) AS yw, COUNT(*) AS cnt " +
                   "FROM machine_session WHERE member_id = :memberId AND recorded_at >= :since " +
                   "GROUP BY yw ORDER BY yw",
           nativeQuery = true)
    List<Object[]> weeklySessionCounts(@Param("memberId") Long memberId, @Param("since") LocalDateTime since);

    // For pie chart: sessions by machineType
    @Query(value = "SELECT machine_type, COUNT(*) AS cnt FROM machine_session GROUP BY machine_type",
           nativeQuery = true)
    List<Object[]> countByMachineType();

    // For low-activity alert: count sessions per member in window
    @Query(value = "SELECT member_id, COALESCE(member_email, ''), COUNT(*) AS cnt " +
                   "FROM machine_session WHERE recorded_at >= :since " +
                   "GROUP BY member_id, member_email",
           nativeQuery = true)
    List<Object[]> sessionCountsPerMemberSince(@Param("since") LocalDateTime since);

    // Distinct dates member had a session
    @Query(value = "SELECT DISTINCT DATE(recorded_at) FROM machine_session WHERE member_id = :memberId " +
                   "ORDER BY DATE(recorded_at) DESC",
           nativeQuery = true)
    List<java.sql.Date> findDistinctSessionDatesForMember(@Param("memberId") Long memberId);
}
