package com.veltro.auth.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory sliding-window rate limiter for login/register attempts per IP.
 *
 * Uses a ConcurrentHashMap<IP, Deque<timestamps>> to track request times.
 * On each call, evicts timestamps older than the window, then checks the count.
 *
 * Thread-safe: synchronized on the IP-specific Deque.
 * Memory: bounded by maxWindowMs (old entries evicted); max entries ≈ maxAttempts
 * per unique IP, which is trivially small even for thousands of clients.
 *
 * For multi-instance deployments this must be replaced with Redis-backed
 * rate limiting (e.g. Bucket4j + RedisRateLimiter).
 */
@Service
public class LoginRateLimiterService {

    private final ConcurrentHashMap<String, Deque<Long>> attempts = new ConcurrentHashMap<>();

    @Value("${auth.rate-limit.max-attempts:10}")
    private int maxAttempts;

    @Value("${auth.rate-limit.window-ms:60000}")
    private long windowMs;

    /**
     * Records an attempt for the given IP and throws if the limit is exceeded.
     *
     * @param ip the client IP address (from X-Forwarded-For or RemoteAddr)
     * @throws TooManyAttemptsException when the per-minute limit is exceeded
     */
    public void checkAndRecord(String ip) {
        long now = System.currentTimeMillis();
        long cutoff = now - windowMs;

        Deque<Long> window = attempts.computeIfAbsent(ip, k -> new ArrayDeque<>());

        synchronized (window) {
            // Evict timestamps outside the sliding window
            while (!window.isEmpty() && window.peekFirst() < cutoff) {
                window.pollFirst();
            }

            if (window.size() >= maxAttempts) {
                throw new TooManyAttemptsException(
                        "Too many login attempts from this IP. Please wait 1 minute before trying again.");
            }

            window.addLast(now);
        }
    }

    public static class TooManyAttemptsException extends RuntimeException {
        public TooManyAttemptsException(String message) { super(message); }
    }
}
