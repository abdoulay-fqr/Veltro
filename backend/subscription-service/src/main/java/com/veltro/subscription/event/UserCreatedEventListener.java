package com.veltro.subscription.event;

import com.veltro.subscription.config.RabbitMQConfig;
import com.veltro.subscription.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserCreatedEventListener {

    private final SubscriptionService subscriptionService;

    @RabbitListener(queues = RabbitMQConfig.SUB_USER_CREATED_QUEUE)
    public void onUserCreated(UserCreatedEvent event) {
        log.info("Received UserCreatedEvent: userId={}, role={}", event.getUserId(), event.getRole());

        // Only auto-create TRIAL for MEMBER role (not COACH or ADMIN)
        if (!"MEMBER".equals(event.getRole())) {
            log.debug("Skipping TRIAL creation for role={}", event.getRole());
            return;
        }

        subscriptionService.createTrial(event.getUserId(), event.getIdentifier());
    }
}
