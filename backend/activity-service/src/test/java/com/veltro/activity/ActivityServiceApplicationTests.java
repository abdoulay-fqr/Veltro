package com.veltro.activity;

import com.veltro.activity.client.NfcClient;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.Clock;

@SpringBootTest
class ActivityServiceApplicationTests extends BaseIntegrationTest {

    @MockitoBean RabbitTemplate rabbitTemplate;
    @MockitoBean Clock clock;
    @MockitoBean NfcClient nfcClient;

    @Test
    void contextLoads() {}
}
