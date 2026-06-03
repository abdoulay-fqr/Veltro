package com.veltro.subscription;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.Clock;

/**
 * Context-loads smoke test. Extends BaseIntegrationTest so it shares
 * the single Testcontainers MySQL and DynamicPropertySource — avoids
 * having two competing @Container/@DynamicPropertySource setups in the
 * same Surefire JVM, which caused HikariPool "Connection refused" errors
 * in subsequent test classes.
 */
@SpringBootTest
class SubscriptionServiceApplicationTests extends BaseIntegrationTest {

    @MockitoBean RabbitTemplate rabbitTemplate;
    @MockitoBean Clock clock;

    @Test
    void contextLoads() {}
}
