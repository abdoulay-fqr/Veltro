package com.veltro.subscription;

import com.veltro.subscription.dto.CreateSubscriptionRequest;
import com.veltro.subscription.dto.SubscriptionResponse;
import com.veltro.subscription.entity.PaymentMethod;
import com.veltro.subscription.entity.Plan;
import com.veltro.subscription.entity.SubscriptionStatus;
import com.veltro.subscription.event.UserCreatedEvent;
import com.veltro.subscription.event.UserCreatedEventListener;
import com.veltro.subscription.repository.PaymentRecordRepository;
import com.veltro.subscription.repository.SubscriptionRepository;
import com.veltro.subscription.service.SchedulerService;
import com.veltro.subscription.service.SubscriptionService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Phase 3 end-to-end integration test.
 *
 * Chain: Register member → TRIAL auto-created → upgrade to MONTHLY
 *        → pause → expire (mock clock) → SubscriptionExpiring event → tagged subscription-complete
 */
class Phase3IntegrationTest extends BaseIntegrationTest {

    @Autowired private SubscriptionService subscriptionService;
    @Autowired private SchedulerService schedulerService;
    @Autowired private UserCreatedEventListener listener;
    @Autowired private SubscriptionRepository subscriptionRepo;
    @Autowired private PaymentRecordRepository paymentRecordRepo;

    @MockitoBean private RabbitTemplate rabbitTemplate;
    @MockitoBean private Clock clock;

    private void fixClock(String date) {
        when(clock.instant()).thenReturn(Instant.parse(date + "T00:00:00Z"));
        when(clock.getZone()).thenReturn(ZoneOffset.UTC);
    }

    @Test
    void fullPhase3Chain() {
        // ── Step 1: Member registers → UserCreated event → TRIAL auto-created ─────
        fixClock("2026-07-01");
        listener.onUserCreated(new UserCreatedEvent(5001L, "MEMBER", "phase3@veltro.com"));

        var subs = subscriptionRepo.findByMemberIdOrderByCreatedAtDesc(5001L);
        assertThat(subs).hasSize(1);
        assertThat(subs.get(0).getPlan()).isEqualTo(Plan.TRIAL);
        assertThat(subs.get(0).getStatus()).isEqualTo(SubscriptionStatus.ACTIVE);

        // ── Step 2: Cancel TRIAL, upgrade to MONTHLY ─────────────────────────────
        subscriptionService.cancel(subs.get(0).getId());

        var req = new CreateSubscriptionRequest();
        req.setMemberId(5001L);
        req.setMemberEmail("phase3@veltro.com");
        req.setPlan(Plan.MONTHLY);
        req.setPaymentMethod(PaymentMethod.CARD);
        req.setAutoRenew(false);
        SubscriptionResponse monthly = subscriptionService.create(req);
        assertThat(monthly.getPlan()).isEqualTo(Plan.MONTHLY);
        assertThat(monthly.getPlanPrice()).isEqualByComparingTo("39.00");

        // Payment record was created
        var invoices = paymentRecordRepo.findBySubscriptionIdOrderByPaidAtDesc(monthly.getId());
        assertThat(invoices).hasSize(1);
        assertThat(invoices.get(0).getAmount()).isEqualByComparingTo("39.00");

        // ── Step 3: Pause subscription ────────────────────────────────────────────
        subscriptionService.pause(monthly.getId());
        var paused = subscriptionRepo.findById(monthly.getId()).orElseThrow();
        assertThat(paused.getStatus()).isEqualTo(SubscriptionStatus.PAUSED);

        // ── Step 4: Resume (5 days paused) ───────────────────────────────────────
        fixClock("2026-07-06");
        subscriptionService.resume(monthly.getId());
        var resumed = subscriptionRepo.findById(monthly.getId()).orElseThrow();
        assertThat(resumed.getStatus()).isEqualTo(SubscriptionStatus.ACTIVE);
        // End date extended by 5 days: Jul 31 + 5 = Aug 05 (MONTHLY = 30 days; Jul 01 + 30 = Jul 31)
        assertThat(resumed.getEndDate()).isEqualTo(java.time.LocalDate.of(2026, 8, 5));

        // ── Step 5: Scheduler runs — subscription in 7-day window → event published
        fixClock("2026-07-31"); // 6 days before Aug 06
        schedulerService.processExpiring();

        ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
        verify(rabbitTemplate, atLeastOnce()).convertAndSend(
                eq("veltro.subscription.exchange"),
                eq("subscription.expiring"),
                eventCaptor.capture()
        );

        // ── Step 6: After expiry date, subscription marked EXPIRED ───────────────
        fixClock("2026-08-10"); // past end date
        schedulerService.processExpiring();

        var expired = subscriptionRepo.findById(monthly.getId()).orElseThrow();
        assertThat(expired.getStatus()).isEqualTo(SubscriptionStatus.EXPIRED);
    }
}
