package com.veltro.messaging.event;

import com.veltro.messaging.entity.SenderRole;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageReceivedEvent {
    private Long messageId;
    private Long conversationId;
    private Long senderId;
    private String senderName;
    private SenderRole senderRole;
    private Long recipientId;
    private String contentPreview;   // max 50 chars
    private LocalDateTime sentAt;
}
