package com.veltro.notification.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MemberWarningEvent {
    private Long memberId;
    private String memberEmail;
    private int warningLevel;
    private long absenceCount;
    private String courseName;
}
