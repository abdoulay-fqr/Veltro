package com.veltro.booking.client;

import com.veltro.booking.exception.ServiceUnavailableException;
import com.veltro.common.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

@FeignClient(
        name = "subscription-client",
        url = "${clients.subscription.url}",
        fallbackFactory = SubscriptionClient.SubscriptionClientFallback.class
)
public interface SubscriptionClient {

    @GetMapping("/api/v1/subscriptions/{memberId}")
    ApiResponse<Map<String, Object>> getActiveSubscription(@PathVariable("memberId") Long memberId);

    @Component
    @Slf4j
    class SubscriptionClientFallback implements FallbackFactory<SubscriptionClient> {
        @Override
        public SubscriptionClient create(Throwable cause) {
            return memberId -> {
                log.error("Subscription service unavailable for memberId={}: {}", memberId, cause.getMessage());
                throw new ServiceUnavailableException(
                        "Subscription service is temporarily unavailable. Please try again shortly.");
            };
        }
    }
}
