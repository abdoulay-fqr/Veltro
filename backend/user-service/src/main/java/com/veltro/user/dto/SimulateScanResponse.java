package com.veltro.user.dto;

import com.veltro.user.entity.MemberProfile;
import com.veltro.user.entity.NfcCard;
import com.veltro.user.entity.NfcCardStatus;
import lombok.Getter;

@Getter
public class SimulateScanResponse {

    private final String cardUid;
    private final NfcCardStatus cardStatus;
    private final Long memberId;        // member_profile.id
    private final Long userId;          // auth-service user_id
    private final String firstname;
    private final String lastname;
    private final String phone;
    private final String avatarUrl;
    private final String accountStatus;

    public SimulateScanResponse(NfcCard card) {
        MemberProfile m = card.getMemberProfile();
        this.cardUid = card.getCardUid();
        this.cardStatus = card.getStatus();
        this.memberId = m.getId();
        this.userId = m.getUserId();
        this.firstname = m.getFirstname();
        this.lastname = m.getLastname();
        this.phone = m.getPhone();
        this.avatarUrl = m.getAvatarUrl();
        this.accountStatus = m.getStatus().name();
    }
}