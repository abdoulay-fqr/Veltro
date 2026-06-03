package com.veltro.activity.repository;

import com.veltro.activity.entity.Direction;
import com.veltro.activity.entity.GymEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface GymEntryRepository extends JpaRepository<GymEntry, Long> {

    Page<GymEntry> findByMemberIdOrderByTimestampDesc(Long memberId, Pageable pageable);

    List<GymEntry> findByMemberIdAndTimestampBetweenOrderByTimestampDesc(
            Long memberId, LocalDateTime from, LocalDateTime to);

    List<GymEntry> findTop20ByOrderByTimestampDesc();

    // Find all IN entries for a cardUid whose sessionId does not appear as any OUT entry
    @Query("SELECT e FROM GymEntry e WHERE e.cardUid = :cardUid AND e.direction = 'IN' " +
           "AND e.sessionId NOT IN " +
           "  (SELECT e2.sessionId FROM GymEntry e2 WHERE e2.direction = 'OUT' AND e2.sessionId IS NOT NULL) " +
           "ORDER BY e.timestamp DESC")
    List<GymEntry> findUnmatchedInEntries(@Param("cardUid") String cardUid, Pageable pageable);

    @Query(value = "SELECT HOUR(timestamp) AS hour, COUNT(*) AS cnt FROM gym_entry " +
                   "WHERE direction = 'IN' GROUP BY HOUR(timestamp) ORDER BY HOUR(timestamp)",
           nativeQuery = true)
    List<Object[]> countEntriesByHour();

    @Query(value = "SELECT COUNT(*) FROM gym_entry WHERE direction = 'IN' AND DATE(timestamp) = CURDATE()",
           nativeQuery = true)
    long countTodayEntries();

    @Query(value = "SELECT COUNT(*) FROM gym_entry WHERE direction = 'IN' AND YEARWEEK(timestamp, 1) = YEARWEEK(NOW(), 1)",
           nativeQuery = true)
    long countWeeklyEntries();

    // Distinct dates member was present (for streak calculation)
    @Query(value = "SELECT DISTINCT DATE(timestamp) FROM gym_entry WHERE member_id = :memberId " +
                   "ORDER BY DATE(timestamp) DESC",
           nativeQuery = true)
    List<java.sql.Date> findDistinctDatesForMember(@Param("memberId") Long memberId);

    // Current occupancy: IN without matching OUT
    @Query(value = "SELECT COUNT(*) FROM gym_entry WHERE direction = 'IN' " +
                   "AND session_id NOT IN " +
                   "  (SELECT session_id FROM gym_entry WHERE direction = 'OUT' AND session_id IS NOT NULL)",
           nativeQuery = true)
    long countCurrentOccupancy();

    // Members active in last N days (for low-activity alert)
    @Query(value = "SELECT DISTINCT member_id FROM gym_entry WHERE timestamp >= :since",
           nativeQuery = true)
    List<Long> findDistinctMemberIdsSince(@Param("since") LocalDateTime since);
}
