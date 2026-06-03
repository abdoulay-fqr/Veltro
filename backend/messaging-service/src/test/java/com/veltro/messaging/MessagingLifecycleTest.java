package com.veltro.messaging;

import com.veltro.messaging.dto.*;
import com.veltro.messaging.entity.SenderRole;
import com.veltro.messaging.service.ConversationCommandService;
import com.veltro.messaging.service.ConversationQueryService;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.assertj.core.api.Assertions.assertThat;

class MessagingLifecycleTest extends BaseIntegrationTest {

    @Autowired private ConversationCommandService commandService;
    @Autowired private ConversationQueryService queryService;
    @MockitoBean private RabbitTemplate rabbitTemplate;

    @Test
    void memberStartsConversationWithCoach() {
        var req = new StartConversationRequest();
        req.setCoachId(201L);

        ConversationResponse conv = commandService.startConversation(101L, req);
        assertThat(conv.getId()).isNotNull();
        assertThat(conv.getMemberId()).isEqualTo(101L);
        assertThat(conv.getCoachId()).isEqualTo(201L);

        // Starting again returns existing conversation
        ConversationResponse conv2 = commandService.startConversation(101L, req);
        assertThat(conv2.getId()).isEqualTo(conv.getId());
    }

    @Test
    void sendMessageIncreasesUnreadForRecipient() {
        var startReq = new StartConversationRequest();
        startReq.setCoachId(202L);
        ConversationResponse conv = commandService.startConversation(102L, startReq);

        var sendReq = new SendMessageRequest();
        sendReq.setContent("Hello coach!");

        MessageResponse msg = commandService.sendMessage(conv.getId(), 102L, "MEMBER", sendReq);
        assertThat(msg.getSenderRole()).isEqualTo(SenderRole.MEMBER);
        assertThat(msg.getContent()).isEqualTo("Hello coach!");

        // Coach should have 1 unread
        int coachUnread = queryService.getUnreadCount(202L, "COACH");
        assertThat(coachUnread).isEqualTo(1);
    }

    @Test
    void markAsReadResetsUnreadCount() {
        var startReq = new StartConversationRequest();
        startReq.setCoachId(203L);
        ConversationResponse conv = commandService.startConversation(103L, startReq);

        var sendReq = new SendMessageRequest();
        sendReq.setContent("Hi!");
        commandService.sendMessage(conv.getId(), 103L, "MEMBER", sendReq);
        commandService.sendMessage(conv.getId(), 103L, "MEMBER", sendReq);

        assertThat(queryService.getUnreadCount(203L, "COACH")).isEqualTo(2);

        commandService.markConversationRead(conv.getId(), 203L, "COACH");

        assertThat(queryService.getUnreadCount(203L, "COACH")).isEqualTo(0);
    }

    @Test
    void messagesReturnedOldestFirst() {
        var startReq = new StartConversationRequest();
        startReq.setCoachId(204L);
        ConversationResponse conv = commandService.startConversation(104L, startReq);

        for (int i = 1; i <= 3; i++) {
            var req = new SendMessageRequest();
            req.setContent("Message " + i);
            commandService.sendMessage(conv.getId(), 104L, "MEMBER", req);
        }

        var page = queryService.getMessages(conv.getId(), 104L, "MEMBER", PageRequest.of(0, 10));
        assertThat(page.getContent()).hasSize(3);
        assertThat(page.getContent().get(0).getContent()).isEqualTo("Message 1");
        assertThat(page.getContent().get(2).getContent()).isEqualTo("Message 3");
    }
}
