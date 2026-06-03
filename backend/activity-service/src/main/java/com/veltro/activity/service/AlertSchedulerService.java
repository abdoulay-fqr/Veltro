package com.veltro.activity.service;

import com.veltro.activity.config.RabbitMQConfig;
import com.veltro.activity.event.LowActivityAlertEvent;
import com.veltro.activity.repository.MachineSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;

@Service
@RequiredArgsConstructor
@Slf4j
public class AlertSchedulerService {

    private final MachineSessionRepository sessionRepo;
    private final RabbitTemplate rabbitTemplate;
    private final Clock clock;

    @Value("${activity.low-activity-session-threshold:2}")
    private int threshold;

    @Scheduled(cron = "0 0 8 * * MON")
    public void checkLowActivity() {
        LocalDate today = LocalDate.now(clock);
        LocalDate weekStart = today.with(TemporalAdjusters.previous(DayOfWeek.MONDAY));
        LocalDateTime since = weekStart.atStartOfDay();

        log.info("LowActivityAlert scheduler running for week starting {}", weekStart);

        var sessionCounts = sessionRepo.sessionCountsPerMemberSince(since);
        int alerts = 0;

        for (Object[] row : sessionCounts) {
            Long memberId = ((Number) row[0]).longValue();
            String memberEmail = row[1] != null ? row[1].toString() : null;
            int count = ((Number) row[2]).intValue();

            if (count < threshold) {
                rabbitTemplate.convertAndSend(
                        RabbitMQConfig.ACTIVITY_EXCHANGE,
                        RabbitMQConfig.LOW_ACTIVITY_ALERT_ROUTING_KEY,
                        new LowActivityAlertEvent(memberId, memberEmail, count, weekStart));

                log.info("LowActivityAlert sent: memberId={}, sessions={}", memberId, count);
                alerts++;
            }
        }

        log.info("LowActivityAlert scheduler complete: {} alerts sent", alerts);
    }
}
