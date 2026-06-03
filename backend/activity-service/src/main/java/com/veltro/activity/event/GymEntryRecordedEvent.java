package com.veltro.activity.event;

import com.veltro.activity.entity.Direction;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GymEntryRecordedEvent {
    private Long entryId;
    private Long memberId;
    private String memberName;
    private String cardUid;
    private Direction direction;
    private LocalDateTime timestamp;
    private String sessionId;
}
