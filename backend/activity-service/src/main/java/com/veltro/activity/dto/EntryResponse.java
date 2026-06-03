package com.veltro.activity.dto;

import com.veltro.activity.entity.Direction;
import com.veltro.activity.entity.GymEntry;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EntryResponse {

    private Long id;
    private Long memberId;
    private String memberName;
    private String cardUid;
    private Direction direction;
    private LocalDateTime timestamp;
    private String sessionId;

    public static EntryResponse from(GymEntry e) {
        EntryResponse r = new EntryResponse();
        r.setId(e.getId());
        r.setMemberId(e.getMemberId());
        r.setMemberName(e.getMemberName());
        r.setCardUid(e.getCardUid());
        r.setDirection(e.getDirection());
        r.setTimestamp(e.getTimestamp());
        r.setSessionId(e.getSessionId());
        return r;
    }
}
