package com.veltro.booking.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WaitlistPromotedEvent {
    private Long courseId;
    private String courseName;
    private LocalDateTime courseDateTime;
    private Long memberId;
    private String memberEmail;
}
