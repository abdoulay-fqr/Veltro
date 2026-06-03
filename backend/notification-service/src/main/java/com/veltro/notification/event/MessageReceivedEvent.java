package com.veltro.notification.event;

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
    private String senderRole;
    private Long recipientId;
    private String contentPreview;
    private LocalDateTime sentAt;
}
