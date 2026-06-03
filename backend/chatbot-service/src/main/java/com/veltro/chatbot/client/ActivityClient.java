package com.veltro.chatbot.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient(
        name = "activity-context-client",
        url = "${clients.activity.url}",
        fallbackFactory = ActivityClient.Fallback.class
)
public interface ActivityClient {

    @GetMapping("/api/v1/activity/entries/{memberId}")
    Map<String, Object> getMemberEntries(
            @PathVariable("memberId") Long memberId,
            @RequestParam("from") String from,
            @RequestParam("size") int size);

    @Component
    @Slf4j
    class Fallback implements FallbackFactory<ActivityClient> {
        @Override
        public ActivityClient create(Throwable cause) {
            return (memberId, from, size) -> {
                log.warn("[ChatbotCtx] activity-service unavailable for memberId={}: {}", memberId, cause.getMessage());
                return null;
            };
        }
    }
}
