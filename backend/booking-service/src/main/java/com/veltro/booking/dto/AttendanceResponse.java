package com.veltro.booking.dto;

import com.veltro.booking.entity.Attendance;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AttendanceResponse {

    private Long id;
    private Long courseId;
    private Long memberId;
    private String memberEmail;
    private boolean present;
    private LocalDateTime markedAt;
    private Long markedByCoachId;

    public static AttendanceResponse from(Attendance a) {
        AttendanceResponse r = new AttendanceResponse();
        r.setId(a.getId());
        r.setCourseId(a.getCourseId());
        r.setMemberId(a.getMemberId());
        r.setMemberEmail(a.getMemberEmail());
        r.setPresent(a.isPresent());
        r.setMarkedAt(a.getMarkedAt());
        r.setMarkedByCoachId(a.getMarkedByCoachId());
        return r;
    }
}
