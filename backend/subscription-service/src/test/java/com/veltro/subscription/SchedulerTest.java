package com.veltro.subscription;

import com.veltro.subscription.dto.CreateSubscriptionRequest;
import com.veltro.subscription.entity.PaymentMethod;
import com.veltro.subscription.entity.Plan;
import com.veltro.subscription.entity.Subscription;
import com.veltro.subscription.entity.SubscriptionStatus;
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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class SchedulerTest extends BaseIntegrationTest {

    @Autowired private SchedulerService schedulerService;
    @Autowired private SubscriptionService subscriptionService;
    @Autowired private SubscriptionRepository subscriptionRepo;
    @Autowired private PaymentRecordRepository paymentRecordRepo;

    @MockitoBean private RabbitTemplate rabbitTemplate;
    @MockitoBean private Clock clock;

    private void fixClock(String date) {
        when(clock.instant()).thenReturn(Instant.parse(date + "T00:00:00Z"));
        when(clock.getZone()).thenReturn(ZoneOffset.UTC);
    }

    @Test
    void expiredSubscriptionGetsMarkedExpired() {
        fixClock("2026-06-01");
        var req = new CreateSubscriptionRequest();
        req.setMemberId(2001L);
        req.setMemberEmail("sched1@test.com");
        req.setPlan(Plan.TRIAL);
        req.setPaymentMethod(PaymentMethod.CARD);
        var sub = subscriptionService.create(req);

        // Move clock past end date
        fixClock("2026-06-20");
        schedulerService.processExpiring();

        Subscription updated = subscriptionRepo.findById(sub.getId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(SubscriptionStatus.EXPIRED);
    }

    @Test
    void autoRenewCreatesNewPaymentRecord() {
        fixClock("2026-06-01");
        var req = new CreateSubscriptionRequest();
        req.setMemberId(2002L);
        req.setMemberEmail("sched2@test.com");
        req.setPlan(Plan.MONTHLY);
        req.setAutoRenew(true);
        req.setPaymentMethod(PaymentMethod.CARD);
        var sub = subscriptionService.create(req);

        long initialPayments = paymentRecordRepo.findBySubscriptionIdOrderByPaidAtDesc(sub.getId()).size();

        // Move clock past end date
        fixClock("2026-07-02");
        schedulerService.processExpiring();

        List<?> payments = paymentRecordRepo.findBySubscriptionIdOrderByPaidAtDesc(sub.getId());
        assertThat(payments).hasSizeGreaterThan((int) initialPayments);

        Subscription renewed = subscriptionRepo.findById(sub.getId()).orElseThrow();
        assertThat(renewed.getStatus()).isEqualTo(SubscriptionStatus.ACTIVE);
    }

    @Test
    void expiringSubscriptionPublishesRabbitEvent() {
        fixClock("2026-06-01");
        var req = new CreateSubscriptionRequest();
        req.setMemberId(2003L);
        req.setMemberEmail("sched3@test.com");
        req.setPlan(Plan.MONTHLY);
        req.setPaymentMethod(PaymentMethod.CARD);
        subscriptionService.create(req);

        // Move clock to 5 days before expiry (within 7-day warning window)
        fixClock("2026-06-26");
        schedulerService.processExpiring();

        verify(rabbitTemplate, atLeastOnce()).convertAndSend(
                eq("veltro.subscription.exchange"),
                eq("subscription.expiring"),
                any()
        );
    }
}
