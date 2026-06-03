package com.veltro.booking.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MemberWarningEvent {
    private Long memberId;
    private String memberEmail;
    private int warningLevel;   // 1, 2, or 3
    private long absenceCount;
    private String courseName;
}
