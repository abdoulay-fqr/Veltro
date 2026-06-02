package com.veltro.subscription.service;

import com.veltro.subscription.entity.Subscription;
import com.veltro.subscription.entity.SubscriptionStatus;
import com.veltro.subscription.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SchedulerService {

    private final SubscriptionRepository subscriptionRepo;
    private final SubscriptionService subscriptionService;
    private final Clock clock;

    @Scheduled(cron = "0 0 0 * * *")
    public void processExpiring() {
        LocalDate today = LocalDate.now(clock);
        LocalDate in7Days = today.plusDays(7);

        log.info("Scheduler running for date={}", today);

        // 1. Find subscriptions that have already expired → auto-renew or mark EXPIRED
        List<Subscription> expired = subscriptionRepo.findByStatusAndEndDateBefore(
                SubscriptionStatus.ACTIVE, today);
        for (Subscription sub : expired) {
            if (sub.isAutoRenew() && sub.getPlan().getPrice().compareTo(java.math.BigDecimal.ZERO) >= 0) {
                subscriptionService.renewSubscription(sub);
            } else {
                subscriptionService.expireSubscription(sub);
            }
        }

        // 2. Find subscriptions expiring within the next 7 days → notify
        List<Subscription> expiringSoon = subscriptionRepo.findByStatusAndEndDateBetween(
                SubscriptionStatus.ACTIVE, today, in7Days);
        for (Subscription sub : expiringSoon) {
            subscriptionService.publishExpiringEvent(sub);
        }

        log.info("Scheduler: processed {} expired, {} expiring-soon", expired.size(), expiringSoon.size());
    }
}
