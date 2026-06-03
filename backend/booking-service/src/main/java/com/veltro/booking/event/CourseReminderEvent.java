package com.veltro.booking.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseReminderEvent {
    private Long courseId;
    private String courseName;
    private LocalDateTime courseDateTime;
    private String room;
    private List<Long> bookedMemberIds;
    private List<String> bookedMemberEmails;
}
