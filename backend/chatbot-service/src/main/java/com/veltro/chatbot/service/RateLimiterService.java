package com.veltro.chatbot.service;

import com.veltro.chatbot.exception.RateLimitException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimiterService {

    private final ConcurrentHashMap<Long, Deque<Long>> timestamps = new ConcurrentHashMap<>();

    @Value("${chatbot.rate-limit.max-requests-per-hour:20}")
    private int maxRequestsPerHour;

    private static final long ONE_HOUR_MS = 3_600_000L;

    public void checkAndRecord(Long userId) {
        long now = System.currentTimeMillis();
        long cutoff = now - ONE_HOUR_MS;

        Deque<Long> userTimestamps = timestamps.computeIfAbsent(userId, k -> new ArrayDeque<>());

        synchronized (userTimestamps) {
            while (!userTimestamps.isEmpty() && userTimestamps.peekFirst() < cutoff) {
                userTimestamps.pollFirst();
            }
            if (userTimestamps.size() >= maxRequestsPerHour) {
                throw new RateLimitException(
                        "Rate limit exceeded: maximum " + maxRequestsPerHour +
                        " messages per hour. Please wait before sending another message.");
            }
            userTimestamps.addLast(now);
        }
    }
}
