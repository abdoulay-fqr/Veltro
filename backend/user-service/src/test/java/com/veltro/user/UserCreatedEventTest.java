package com.veltro.user;

import com.veltro.user.dto.CreateCoachRequest;
import com.veltro.user.dto.CreateMemberRequest;
import com.veltro.user.event.UserCreatedEvent;
import com.veltro.user.service.CoachService;
import com.veltro.user.service.MemberService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

class UserCreatedEventTest extends BaseIntegrationTest {

    @Autowired private MemberService memberService;
    @Autowired private CoachService coachService;

    @MockitoBean
    private RabbitTemplate rabbitTemplate;

    @Test
    void memberCreationPublishesUserCreatedEvent() {
        CreateMemberRequest req = new CreateMemberRequest();
        req.setUserId(6001L);
        req.setIdentifier("event-member@test.com");
        req.setFirstname("Event");
        req.setLastname("Member");
        memberService.create(req);

        ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
        verify(rabbitTemplate).convertAndSend(
                eq("veltro.user.exchange"),
                eq("user.created"),
                eventCaptor.capture()
        );

        UserCreatedEvent event = (UserCreatedEvent) eventCaptor.getValue();
        assertThat(event.getUserId()).isEqualTo(6001L);
        assertThat(event.getRole()).isEqualTo("MEMBER");
        assertThat(event.getIdentifier()).isEqualTo("event-member@test.com");
    }

    @Test
    void coachCreationPublishesUserCreatedEvent() {
        CreateCoachRequest req = new CreateCoachRequest();
        req.setUserId(6002L);
        req.setIdentifier("event-coach@test.com");
        req.setFirstname("Event");
        req.setLastname("Coach");
        coachService.create(req);

        ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
        verify(rabbitTemplate).convertAndSend(
                eq("veltro.user.exchange"),
                eq("user.created"),
                eventCaptor.capture()
        );

        UserCreatedEvent event = (UserCreatedEvent) eventCaptor.getValue();
        assertThat(event.getUserId()).isEqualTo(6002L);
        assertThat(event.getRole()).isEqualTo("COACH");
        assertThat(event.getIdentifier()).isEqualTo("event-coach@test.com");
    }
}
