package com.veltro.user.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NfcCardActivatedEvent {

    private Long cardId;
    private String cardUid;
    private Long memberProfileId;
    private Long userId;
    private String memberFullName;
}
