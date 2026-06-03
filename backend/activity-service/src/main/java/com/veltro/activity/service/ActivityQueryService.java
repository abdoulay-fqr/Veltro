package com.veltro.activity.service;

import com.veltro.activity.dto.AdminStatsResponse;
import com.veltro.activity.dto.EntryResponse;
import com.veltro.activity.dto.MemberStatsResponse;
import com.veltro.activity.dto.SessionResponse;
import com.veltro.activity.repository.GymEntryRepository;
import com.veltro.activity.repository.MachineSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ActivityQueryService {

    private final GymEntryRepository entryRepo;
    private final MachineSessionRepository sessionRepo;
    private final Clock clock;

    // ── Entry queries ─────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public Page<EntryResponse> getEntriesByMember(Long memberId, LocalDateTime from, LocalDateTime to, Pageable pageable) {
        if (from != null && to != null) {
            return entryRepo.findByMemberIdOrderByTimestampDesc(memberId, pageable)
                    .map(EntryResponse::from);
        }
        return entryRepo.findByMemberIdOrderByTimestampDesc(memberId, pageable).map(EntryResponse::from);
    }

    @Transactional(readOnly = true)
    public List<EntryResponse> getLiveEntries() {
        return entryRepo.findTop20ByOrderByTimestampDesc().stream()
                .map(EntryResponse::from).toList();
    }

    // ── Session queries ───────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public Page<SessionResponse> getSessionsByMember(Long memberId, Pageable pageable) {
        return sessionRepo.findByMemberIdOrderByRecordedAtDesc(memberId, pageable).map(SessionResponse::from);
    }

    // ── Member stats ──────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public MemberStatsResponse getMemberStats(Long memberId) {
        MemberStatsResponse stats = new MemberStatsResponse();
        stats.setMemberId(memberId);

        stats.setWeeklyCalories(safeToLong(sessionRepo.sumWeeklyCaloriesForMember(memberId)));
        stats.setTotalSessions(sessionRepo.countByMemberId(memberId));
        stats.setAvgHeartRate(sessionRepo.avgHeartRateLastNSessions(memberId, 30));
        stats.setCurrentStreak(calculateStreak(memberId));
        stats.setMaxCaloriesInSession(safeToInt(sessionRepo.maxCaloriesForMember(memberId)));
        stats.setMaxDistanceKm(safeToDouble(sessionRepo.maxDistanceForMember(memberId)));
        stats.setMaxDurationMinutes(safeToInt(sessionRepo.maxDurationForMember(memberId)));

        return stats;
    }

    private int calculateStreak(Long memberId) {
        Set<LocalDate> entryDates = entryRepo.findDistinctDatesForMember(memberId).stream()
                .map(java.sql.Date::toLocalDate).collect(Collectors.toSet());
        Set<LocalDate> sessionDates = sessionRepo.findDistinctSessionDatesForMember(memberId).stream()
                .map(java.sql.Date::toLocalDate).collect(Collectors.toSet());

        Set<LocalDate> allActiveDates = new HashSet<>(entryDates);
        allActiveDates.addAll(sessionDates);

        if (allActiveDates.isEmpty()) return 0;

        LocalDate today = LocalDate.now(clock);
        int streak = 0;
        LocalDate check = today;

        while (allActiveDates.contains(check)) {
            streak++;
            check = check.minusDays(1);
        }
        return streak;
    }

    // ── Admin stats ───────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public AdminStatsResponse getAdminStats() {
        AdminStatsResponse stats = new AdminStatsResponse();

        Map<Integer, Long> byHour = new LinkedHashMap<>();
        for (int h = 0; h < 24; h++) byHour.put(h, 0L);

        List<Object[]> rows = entryRepo.countEntriesByHour();
        for (Object[] row : rows) {
            int hour = ((Number) row[0]).intValue();
            long count = ((Number) row[1]).longValue();
            byHour.put(hour, count);
        }
        stats.setEntriesByHour(byHour);

        int peakHour = byHour.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey).orElse(0);
        stats.setPeakHour(peakHour);

        stats.setTodayEntries(entryRepo.countTodayEntries());
        stats.setWeeklyEntries(entryRepo.countWeeklyEntries());
        stats.setCurrentOccupancy(entryRepo.countCurrentOccupancy());

        return stats;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private long safeToLong(Number n) { return n == null ? 0L : n.longValue(); }
    private int safeToInt(Number n)   { return n == null ? 0 : n.intValue(); }
    private double safeToDouble(Number n) { return n == null ? 0.0 : n.doubleValue(); }
}
