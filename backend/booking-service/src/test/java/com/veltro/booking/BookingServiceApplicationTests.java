package com.veltro.booking;

import com.veltro.booking.client.SubscriptionClient;
import com.veltro.booking.client.UserClient;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.Clock;

@SpringBootTest
class BookingServiceApplicationTests extends BaseIntegrationTest {

    @MockitoBean RabbitTemplate rabbitTemplate;
    @MockitoBean Clock clock;
    @MockitoBean SubscriptionClient subscriptionClient;
    @MockitoBean UserClient userClient;

    @Test
    void contextLoads() {}
}
