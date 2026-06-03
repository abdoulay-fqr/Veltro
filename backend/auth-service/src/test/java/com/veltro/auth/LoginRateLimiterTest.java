package com.veltro.auth;

import com.veltro.auth.service.LoginRateLimiterService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LoginRateLimiterTest {

    private LoginRateLimiterService rateLimiter;

    @BeforeEach
    void setUp() {
        rateLimiter = new LoginRateLimiterService();
        ReflectionTestUtils.setField(rateLimiter, "maxAttempts", 3);
        ReflectionTestUtils.setField(rateLimiter, "windowMs", 60_000L);
    }

    @Test
    void allowsAttemptsUnderLimit() {
        assertThatCode(() -> rateLimiter.checkAndRecord("192.168.1.1")).doesNotThrowAnyException();
        assertThatCode(() -> rateLimiter.checkAndRecord("192.168.1.1")).doesNotThrowAnyException();
        assertThatCode(() -> rateLimiter.checkAndRecord("192.168.1.1")).doesNotThrowAnyException();
    }

    @Test
    void blocksOnExceedingLimit() {
        rateLimiter.checkAndRecord("10.0.0.1");
        rateLimiter.checkAndRecord("10.0.0.1");
        rateLimiter.checkAndRecord("10.0.0.1");

        assertThatThrownBy(() -> rateLimiter.checkAndRecord("10.0.0.1"))
                .isInstanceOf(LoginRateLimiterService.TooManyAttemptsException.class)
                .hasMessageContaining("Too many login attempts");
    }

    @Test
    void differentIpsAreTrackedIndependently() {
        rateLimiter.checkAndRecord("10.0.0.2");
        rateLimiter.checkAndRecord("10.0.0.2");
        rateLimiter.checkAndRecord("10.0.0.2");

        // Different IP should still be allowed
        assertThatCode(() -> rateLimiter.checkAndRecord("10.0.0.3")).doesNotThrowAnyException();
    }

    @Test
    void slidingWindowEvictsExpiredEntries() throws InterruptedException {
        LoginRateLimiterService fastLimiter = new LoginRateLimiterService();
        ReflectionTestUtils.setField(fastLimiter, "maxAttempts", 2);
        ReflectionTestUtils.setField(fastLimiter, "windowMs", 100L); // 100ms window

        fastLimiter.checkAndRecord("172.16.0.1");
        fastLimiter.checkAndRecord("172.16.0.1");

        // Exceed limit
        assertThatThrownBy(() -> fastLimiter.checkAndRecord("172.16.0.1"))
                .isInstanceOf(LoginRateLimiterService.TooManyAttemptsException.class);

        // Wait for window to expire
        Thread.sleep(150);

        // Should be allowed again after window passes
        assertThatCode(() -> fastLimiter.checkAndRecord("172.16.0.1")).doesNotThrowAnyException();
    }
}
