package com.veltro.chatbot.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@FeignClient(
        name = "booking-context-client",
        url = "${clients.booking.url}",
        fallbackFactory = BookingClient.Fallback.class
)
public interface BookingClient {

    @GetMapping("/api/v1/bookings/member/{memberId}")
    Map<String, Object> getMemberBookings(
            @PathVariable("memberId") Long memberId,
            @RequestParam("status") String status);

    @Component
    @Slf4j
    class Fallback implements FallbackFactory<BookingClient> {
        @Override
        public BookingClient create(Throwable cause) {
            return (memberId, status) -> {
                log.warn("[ChatbotCtx] booking-service unavailable for memberId={}: {}", memberId, cause.getMessage());
                return null;
            };
        }
    }
}
