package com.veltro.booking;

import com.veltro.booking.client.SubscriptionClient;
import com.veltro.booking.client.UserClient;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Clock;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
class BookingServiceApplicationTests {

    @Container
    static final MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("veltro_booking_test")
            .withUsername("test")
            .withPassword("test");

    @MockitoBean RabbitTemplate rabbitTemplate;
    @MockitoBean Clock clock;
    @MockitoBean SubscriptionClient subscriptionClient;
    @MockitoBean UserClient userClient;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
        registry.add("eureka.client.enabled", () -> "false");
    }

    @Test
    void contextLoads() {}
}
