package com.veltro.chatbot.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

@FeignClient(
        name = "subscription-context-client",
        url = "${clients.subscription.url}",
        fallbackFactory = SubscriptionClient.Fallback.class
)
public interface SubscriptionClient {

    @GetMapping("/api/v1/subscriptions/{memberId}")
    Map<String, Object> getActiveSubscription(@PathVariable("memberId") Long memberId);

    @Component
    @Slf4j
    class Fallback implements FallbackFactory<SubscriptionClient> {
        @Override
        public SubscriptionClient create(Throwable cause) {
            return memberId -> {
                log.warn("[ChatbotCtx] subscription-service unavailable for memberId={}: {}", memberId, cause.getMessage());
                return null;
            };
        }
    }
}
