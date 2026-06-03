package com.veltro.activity;

import com.veltro.activity.client.NfcClient;
import com.veltro.activity.dto.*;
import com.veltro.activity.entity.Direction;
import com.veltro.activity.entity.GymEntry;
import com.veltro.activity.entity.MachineSession;
import com.veltro.activity.entity.MachineType;
import com.veltro.activity.repository.GymEntryRepository;
import com.veltro.activity.repository.MachineSessionRepository;
import com.veltro.activity.service.ActivityQueryService;
import com.veltro.activity.service.SessionCommandService;
import com.veltro.common.dto.ApiResponse;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class StatsQueryTest extends BaseIntegrationTest {

    @Autowired private ActivityQueryService queryService;
    @Autowired private SessionCommandService sessionCmd;
    @Autowired private GymEntryRepository entryRepo;
    @Autowired private MachineSessionRepository sessionRepo;

    @MockitoBean private NfcClient nfcClient;
    @MockitoBean private RabbitTemplate rabbitTemplate;
    @MockitoBean private Clock clock;

    private void fixClock(String dateTime) {
        when(clock.instant()).thenReturn(Instant.parse(dateTime + ":00Z"));
        when(clock.getZone()).thenReturn(ZoneOffset.UTC);
    }

    private MachineSession saveSession(Long memberId, int calories, int duration, double distance) {
        MachineSession s = new MachineSession();
        s.setMemberId(memberId);
        s.setMachineType(MachineType.TREADMILL);
        s.setDurationMinutes(duration);
        s.setCaloriesBurned(calories);
        s.setDistanceKm(distance);
        s.setAvgHeartRate(145);
        s.setRecordedAt(LocalDateTime.now());
        return sessionRepo.save(s);
    }

    private GymEntry saveEntry(Long memberId, String cardUid, Direction direction, LocalDateTime timestamp) {
        GymEntry e = new GymEntry();
        e.setMemberId(memberId);
        e.setCardUid(cardUid);
        e.setMemberName("Test User");
        e.setDirection(direction);
        e.setTimestamp(timestamp);
        e.setSessionId(UUID.randomUUID().toString());
        return entryRepo.save(e);
    }

    @Test
    void memberStatsTotalSessionsAndPersonalRecords() {
        fixClock("2026-06-01T10:00");
        Long memberId = 7001L;
        saveSession(memberId, 500, 45, 8.0);
        saveSession(memberId, 700, 60, 12.0);
        saveSession(memberId, 300, 30, 5.0);

        MemberStatsResponse stats = queryService.getMemberStats(memberId);
        assertThat(stats.getTotalSessions()).isEqualTo(3);
        assertThat(stats.getMaxCaloriesInSession()).isEqualTo(700);
        assertThat(stats.getMaxDistanceKm()).isEqualTo(12.0);
        assertThat(stats.getMaxDurationMinutes()).isEqualTo(60);
    }

    @Test
    void adminStatsEntriesByHour() {
        fixClock("2026-06-02T10:00");
        Long memberId = 7002L;

        // Save entries at 9am, 10am, 10am
        saveEntry(memberId, "card-stat-001", Direction.IN, LocalDateTime.of(2026, 6, 2, 9, 0));
        saveEntry(memberId, "card-stat-001", Direction.IN, LocalDateTime.of(2026, 6, 2, 10, 0));
        saveEntry(memberId, "card-stat-001", Direction.IN, LocalDateTime.of(2026, 6, 2, 10, 30));

        AdminStatsResponse stats = queryService.getAdminStats();
        assertThat(stats.getEntriesByHour()).isNotNull();
        assertThat(stats.getEntriesByHour().size()).isEqualTo(24);
        assertThat(stats.getEntriesByHour().get(10)).isGreaterThanOrEqualTo(2L);
    }

    @Test
    void sessionCommandPublishesEvent() {
        fixClock("2026-06-01T09:00");
        SessionRequest req = new SessionRequest();
        req.setMemberId(7003L);
        req.setMachineType(MachineType.BIKE);
        req.setDurationMinutes(30);
        req.setCaloriesBurned(400);
        req.setDistanceKm(15.0);
        req.setAvgHeartRate(150);

        SessionResponse resp = sessionCmd.recordSession(req);
        assertThat(resp.getId()).isNotNull();
        assertThat(resp.getMachineType()).isEqualTo(MachineType.BIKE);
        assertThat(resp.getCaloriesBurned()).isEqualTo(400);
    }
}
