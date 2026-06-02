package com.veltro.subscription;

import com.veltro.subscription.dto.CreateSubscriptionRequest;
import com.veltro.subscription.dto.SubscriptionResponse;
import com.veltro.subscription.entity.PaymentMethod;
import com.veltro.subscription.entity.Plan;
import com.veltro.subscription.entity.SubscriptionStatus;
import com.veltro.subscription.exception.BusinessRuleException;
import com.veltro.subscription.exception.ConflictException;
import com.veltro.subscription.service.SubscriptionService;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SubscriptionLifecycleTest extends BaseIntegrationTest {

    @Autowired private SubscriptionService subscriptionService;
    @MockitoBean private RabbitTemplate rabbitTemplate;
    @MockitoBean private Clock clock;

    private void fixClock(String date) {
        org.mockito.Mockito.when(clock.instant()).thenReturn(Instant.parse(date + "T00:00:00Z"));
        org.mockito.Mockito.when(clock.getZone()).thenReturn(ZoneOffset.UTC);
    }

    private CreateSubscriptionRequest req(long memberId, Plan plan) {
        CreateSubscriptionRequest r = new CreateSubscriptionRequest();
        r.setMemberId(memberId);
        r.setMemberEmail("test" + memberId + "@veltro.com");
        r.setPlan(plan);
        r.setPaymentMethod(PaymentMethod.CARD);
        return r;
    }

    @Test
    void createMonthlySubscription() {
        fixClock("2026-06-01");
        SubscriptionResponse r = subscriptionService.create(req(1001L, Plan.MONTHLY));
        assertThat(r.getPlan()).isEqualTo(Plan.MONTHLY);
        assertThat(r.getStatus()).isEqualTo(SubscriptionStatus.ACTIVE);
        assertThat(r.getEndDate()).isEqualTo(java.time.LocalDate.of(2026, 7, 1));
        assertThat(r.getPlanPrice()).isEqualByComparingTo("39.00");
    }

    @Test
    void cancelSubscription() {
        fixClock("2026-06-01");
        SubscriptionResponse created = subscriptionService.create(req(1002L, Plan.MONTHLY));
        SubscriptionResponse cancelled = subscriptionService.cancel(created.getId());
        assertThat(cancelled.getStatus()).isEqualTo(SubscriptionStatus.CANCELLED);
    }

    @Test
    void pauseAndResumeSubscription() {
        fixClock("2026-06-01");
        SubscriptionResponse created = subscriptionService.create(req(1003L, Plan.MONTHLY));

        // Pause on day 1
        SubscriptionResponse paused = subscriptionService.pause(created.getId());
        assertThat(paused.getStatus()).isEqualTo(SubscriptionStatus.PAUSED);

        // Resume 10 days later
        fixClock("2026-06-11");
        SubscriptionResponse resumed = subscriptionService.resume(created.getId());
        assertThat(resumed.getStatus()).isEqualTo(SubscriptionStatus.ACTIVE);
        // End date extended by 10 days: 2026-07-01 + 10 days = 2026-07-11
        assertThat(resumed.getEndDate()).isEqualTo(java.time.LocalDate.of(2026, 7, 11));
        assertThat(resumed.getPausedMonthsUsed()).isEqualTo(1);
    }

    @Test
    void pauseLimitExceeded() {
        fixClock("2026-06-01");
        SubscriptionResponse sub = subscriptionService.create(req(1004L, Plan.ANNUAL));

        // First pause/resume cycle
        subscriptionService.pause(sub.getId());
        fixClock("2026-06-15");
        subscriptionService.resume(sub.getId());

        // Second pause/resume cycle
        subscriptionService.pause(sub.getId());
        fixClock("2026-07-01");
        subscriptionService.resume(sub.getId());

        // Third pause attempt should fail
        assertThatThrownBy(() -> subscriptionService.pause(sub.getId()))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Maximum pause limit");
    }

    @Test
    void duplicateActiveMemberThrowsConflict() {
        fixClock("2026-06-01");
        subscriptionService.create(req(1005L, Plan.MONTHLY));
        assertThatThrownBy(() -> subscriptionService.create(req(1005L, Plan.ANNUAL)))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void toggleAutoRenew() {
        fixClock("2026-06-01");
        SubscriptionResponse sub = subscriptionService.create(req(1006L, Plan.MONTHLY));
        assertThat(sub.isAutoRenew()).isFalse();

        SubscriptionResponse toggled = subscriptionService.toggleAutoRenew(sub.getId());
        assertThat(toggled.isAutoRenew()).isTrue();
    }

    @Test
    void trialAutoCreatedForMember() {
        fixClock("2026-06-01");
        SubscriptionResponse trial = subscriptionService.createTrial(1007L, "member1007@test.com");
        assertThat(trial).isNotNull();
        assertThat(trial.getPlan()).isEqualTo(Plan.TRIAL);
        assertThat(trial.getPlanPrice()).isEqualByComparingTo("0.00");
        assertThat(trial.getEndDate()).isEqualTo(java.time.LocalDate.of(2026, 6, 8));
    }
}
