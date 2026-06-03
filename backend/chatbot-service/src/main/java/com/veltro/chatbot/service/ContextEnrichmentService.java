package com.veltro.chatbot.service;

import com.veltro.chatbot.client.ActivityClient;
import com.veltro.chatbot.client.BookingClient;
import com.veltro.chatbot.client.SubscriptionClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContextEnrichmentService {

    private final SubscriptionClient subscriptionClient;
    private final BookingClient bookingClient;
    private final ActivityClient activityClient;

    @Cacheable(value = "memberContext", key = "#memberId")
    public String buildMemberContext(Long memberId) {
        StringBuilder ctx = new StringBuilder("Current member context:\n");

        // --- Subscription (graceful if down) ---
        try {
            Map<String, Object> sub = subscriptionClient.getActiveSubscription(memberId);
            if (sub != null) {
                Object data = sub.get("data");
                if (data instanceof Map<?, ?> d) {
                    ctx.append("- Subscription: ")
                       .append(d.get("plan")).append(" (").append(d.get("status")).append("), ")
                       .append(d.get("daysRemaining")).append(" days remaining\n");
                }
            } else {
                ctx.append("- Subscription: data unavailable\n");
            }
        } catch (Exception e) {
            log.warn("[ContextEnrich] Subscription context skipped for memberId={}: {}", memberId, e.getMessage());
            ctx.append("- Subscription: unavailable\n");
        }

        // --- Upcoming bookings (graceful if down) ---
        try {
            Map<String, Object> bookingsResp = bookingClient.getMemberBookings(memberId, "BOOKED");
            if (bookingsResp != null) {
                Object data = bookingsResp.get("data");
                if (data instanceof List<?> bookings && !bookings.isEmpty()) {
                    ctx.append("- Upcoming bookings:\n");
                    bookings.stream().limit(3).forEach(b -> {
                        if (b instanceof Map<?, ?> booking) {
                            ctx.append("  * Course #").append(booking.get("courseId"))
                               .append(" booked on ").append(booking.get("registeredAt")).append("\n");
                        }
                    });
                } else {
                    ctx.append("- Upcoming bookings: none\n");
                }
            } else {
                ctx.append("- Upcoming bookings: unavailable\n");
            }
        } catch (Exception e) {
            log.warn("[ContextEnrich] Bookings context skipped for memberId={}: {}", memberId, e.getMessage());
            ctx.append("- Upcoming bookings: unavailable\n");
        }

        // --- Today's gym entry (graceful if down) ---
        try {
            String todayStr = LocalDate.now().toString() + "T00:00:00";
            Map<String, Object> entriesResp = activityClient.getMemberEntries(memberId, todayStr, 1);
            if (entriesResp != null) {
                Object data = entriesResp.get("data");
                if (data instanceof Map<?, ?> page) {
                    Object content = page.get("content");
                    if (content instanceof List<?> entries && !entries.isEmpty()) {
                        if (entries.get(0) instanceof Map<?, ?> entry) {
                            ctx.append("- Today's gym visit: Entered at ")
                               .append(entry.get("timestamp")).append("\n");
                        }
                    } else {
                        ctx.append("- Today's gym visit: Not visited today\n");
                    }
                }
            } else {
                ctx.append("- Today's gym visit: unavailable\n");
            }
        } catch (Exception e) {
            log.warn("[ContextEnrich] Activity context skipped for memberId={}: {}", memberId, e.getMessage());
            ctx.append("- Today's gym visit: unavailable\n");
        }

        return ctx.toString();
    }
}
