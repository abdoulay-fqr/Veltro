package com.veltro.subscription;

import com.veltro.subscription.entity.Plan;
import com.veltro.subscription.entity.SubscriptionStatus;
import com.veltro.subscription.event.UserCreatedEvent;
import com.veltro.subscription.event.UserCreatedEventListener;
import com.veltro.subscription.repository.SubscriptionRepository;
import com.veltro.subscription.service.SubscriptionService;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class UserCreatedEventTest extends BaseIntegrationTest {

    @Autowired private UserCreatedEventListener listener;
    @Autowired private SubscriptionRepository subscriptionRepo;

    @MockitoBean private RabbitTemplate rabbitTemplate;
    @MockitoBean private Clock clock;

    @Test
    void memberUserCreatedEventCreatesTrial() {
        when(clock.instant()).thenReturn(Instant.parse("2026-06-01T00:00:00Z"));
        when(clock.getZone()).thenReturn(ZoneOffset.UTC);

        UserCreatedEvent event = new UserCreatedEvent(3001L, "MEMBER", "newmember@test.com");
        listener.onUserCreated(event);

        var subs = subscriptionRepo.findByMemberIdOrderByCreatedAtDesc(3001L);
        assertThat(subs).hasSize(1);
        assertThat(subs.get(0).getPlan()).isEqualTo(Plan.TRIAL);
        assertThat(subs.get(0).getStatus()).isEqualTo(SubscriptionStatus.ACTIVE);
        assertThat(subs.get(0).getMemberEmail()).isEqualTo("newmember@test.com");
    }

    @Test
    void coachUserCreatedEventDoesNotCreateSubscription() {
        when(clock.instant()).thenReturn(Instant.parse("2026-06-01T00:00:00Z"));
        when(clock.getZone()).thenReturn(ZoneOffset.UTC);

        UserCreatedEvent event = new UserCreatedEvent(3002L, "COACH", "coach@test.com");
        listener.onUserCreated(event);

        var subs = subscriptionRepo.findByMemberIdOrderByCreatedAtDesc(3002L);
        assertThat(subs).isEmpty();
    }

    @Test
    void duplicateUserCreatedEventIsIdempotent() {
        when(clock.instant()).thenReturn(Instant.parse("2026-06-01T00:00:00Z"));
        when(clock.getZone()).thenReturn(ZoneOffset.UTC);

        UserCreatedEvent event = new UserCreatedEvent(3003L, "MEMBER", "idem@test.com");
        listener.onUserCreated(event);
        listener.onUserCreated(event);

        var subs = subscriptionRepo.findByMemberIdOrderByCreatedAtDesc(3003L);
        assertThat(subs).hasSize(1);
    }
}
