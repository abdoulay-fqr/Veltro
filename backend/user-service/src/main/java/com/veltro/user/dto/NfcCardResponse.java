package com.veltro.user.dto;

import com.veltro.user.entity.NfcCard;
import com.veltro.user.entity.NfcCardStatus;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class NfcCardResponse {

    private final Long id;
    private final String cardUid;
    private final NfcCardStatus status;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public NfcCardResponse(NfcCard card) {
        this.id = card.getId();
        this.cardUid = card.getCardUid();
        this.status = card.getStatus();
        this.createdAt = card.getCreatedAt();
        this.updatedAt = card.getUpdatedAt();
    }
}