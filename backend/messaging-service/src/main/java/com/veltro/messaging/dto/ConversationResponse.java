package com.veltro.messaging.dto;

import com.veltro.messaging.entity.Conversation;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ConversationResponse {

    private Long id;
    private Long memberId;
    private Long coachId;
    private LocalDateTime lastMessageAt;
    private String lastMessagePreview;
    private int unreadCount;
    private LocalDateTime createdAt;

    public static ConversationResponse from(Conversation c, boolean isCoach) {
        ConversationResponse r = new ConversationResponse();
        r.setId(c.getId());
        r.setMemberId(c.getMemberId());
        r.setCoachId(c.getCoachId());
        r.setLastMessageAt(c.getLastMessageAt());
        r.setLastMessagePreview(c.getLastMessagePreview());
        r.setUnreadCount(isCoach ? c.getUnreadCountCoach() : c.getUnreadCountMember());
        r.setCreatedAt(c.getCreatedAt());
        return r;
    }
}
