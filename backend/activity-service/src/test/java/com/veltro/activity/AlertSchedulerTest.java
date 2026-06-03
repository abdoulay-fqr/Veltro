package com.veltro.activity;

import com.veltro.activity.client.NfcClient;
import com.veltro.activity.entity.MachineSession;
import com.veltro.activity.entity.MachineType;
import com.veltro.activity.event.LowActivityAlertEvent;
import com.veltro.activity.repository.MachineSessionRepository;
import com.veltro.activity.service.AlertSchedulerService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.TemporalAdjusters;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class AlertSchedulerTest extends BaseIntegrationTest {

    @Autowired private AlertSchedulerService scheduler;
    @Autowired private MachineSessionRepository sessionRepo;

    @MockitoBean private NfcClient nfcClient;
    @MockitoBean private RabbitTemplate rabbitTemplate;
    @MockitoBean private Clock clock;

    private void fixClock(String dateTime) {
        when(clock.instant()).thenReturn(Instant.parse(dateTime + ":00Z"));
        when(clock.getZone()).thenReturn(ZoneOffset.UTC);
    }

    private MachineSession saveSession(Long memberId, String email, LocalDateTime recordedAt) {
        MachineSession s = new MachineSession();
        s.setMemberId(memberId);
        s.setMemberEmail(email);
        s.setMachineType(MachineType.WEIGHTS);
        s.setDurationMinutes(30);
        s.setCaloriesBurned(300);
        s.setRecordedAt(recordedAt);
        return sessionRepo.save(s);
    }

    @Test
    void memberWithOneSessionReceivesLowActivityAlert() {
        fixClock("2026-06-08T08:00"); // Monday
        LocalDateTime thisWeek = LocalDateTime.of(2026, 6, 5, 10, 0); // Previous Thursday (within the week)
        saveSession(8001L, "low@test.com", thisWeek);

        scheduler.checkLowActivity();

        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(rabbitTemplate, atLeastOnce()).convertAndSend(
                eq("veltro.activity.exchange"),
                eq("activity.low-activity.alert"),
                captor.capture()
        );

        boolean found = captor.getAllValues().stream()
                .filter(v -> v instanceof LowActivityAlertEvent)
                .map(v -> (LowActivityAlertEvent) v)
                .anyMatch(e -> e.getMemberId().equals(8001L) && e.getSessionCount() == 1);
        assertThat(found).isTrue();
    }

    @Test
    void memberWithTwoSessionsReceivesNoAlert() {
        fixClock("2026-06-08T08:00"); // Monday
        LocalDateTime thisWeek = LocalDateTime.of(2026, 6, 5, 10, 0);
        saveSession(8002L, "active@test.com", thisWeek);
        saveSession(8002L, "active@test.com", thisWeek.plusHours(2));

        scheduler.checkLowActivity();

        // Member 8002 should NOT get an alert (has 2 sessions >= threshold of 2)
        verify(rabbitTemplate, never()).convertAndSend(
                eq("veltro.activity.exchange"),
                eq("activity.low-activity.alert"),
                (Object) argThat(e -> e instanceof LowActivityAlertEvent &&
                        ((LowActivityAlertEvent) e).getMemberId().equals(8002L))
        );
    }
}
