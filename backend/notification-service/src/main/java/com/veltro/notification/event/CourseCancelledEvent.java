package com.veltro.notification.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseCancelledEvent {
    private Long courseId;
    private String courseName;
    private LocalDateTime courseDateTime;
    private Long coachId;
    private List<Long> affectedMemberIds;
    private List<String> affectedMemberEmails;
}
