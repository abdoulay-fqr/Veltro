package com.veltro.messaging.dto;

import com.veltro.messaging.entity.Message;
import com.veltro.messaging.entity.SenderRole;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MessageResponse {

    private Long id;
    private Long conversationId;
    private Long senderId;
    private SenderRole senderRole;
    private String content;
    private LocalDateTime sentAt;
    private LocalDateTime readAt;

    public static MessageResponse from(Message m) {
        MessageResponse r = new MessageResponse();
        r.setId(m.getId());
        r.setConversationId(m.getConversationId());
        r.setSenderId(m.getSenderId());
        r.setSenderRole(m.getSenderRole());
        r.setContent(m.getContent());
        r.setSentAt(m.getSentAt());
        r.setReadAt(m.getReadAt());
        return r;
    }
}
