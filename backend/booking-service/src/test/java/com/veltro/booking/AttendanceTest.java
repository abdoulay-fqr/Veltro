package com.veltro.booking;

import com.veltro.booking.client.SubscriptionClient;
import com.veltro.booking.client.UserClient;
import com.veltro.booking.dto.*;
import com.veltro.booking.entity.CourseLevel;
import com.veltro.booking.service.AttendanceService;
import com.veltro.booking.service.CourseCommandService;
import com.veltro.common.dto.ApiResponse;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AttendanceTest extends BaseIntegrationTest {

    @Autowired private AttendanceService attendanceService;
    @Autowired private CourseCommandService courseCmd;

    @MockitoBean private UserClient userClient;
    @MockitoBean private SubscriptionClient subscriptionClient;
    @MockitoBean private RabbitTemplate rabbitTemplate;
    @MockitoBean private Clock clock;

    private void fixClock(String dateTime) {
        when(clock.instant()).thenReturn(Instant.parse(dateTime + ":00Z"));
        when(clock.getZone()).thenReturn(ZoneOffset.UTC);
    }

    private Long createPastCourse(String pastDateTime) {
        fixClock("2026-05-01T08:00"); // course is in the "future" when we create it
        CreateCourseRequest req = new CreateCourseRequest();
        req.setCoachId(8001L);
        req.setName("Fitness Class");
        req.setDateTime(java.time.LocalDateTime.parse(pastDateTime));
        req.setDurationMinutes(60);
        req.setCapacity(20);
        req.setLevel(CourseLevel.INTERMEDIATE);
        return courseCmd.create(req).getId();
    }

    private MarkAttendanceRequest attendanceReq(Long courseId, boolean... presences) {
        MarkAttendanceRequest req = new MarkAttendanceRequest();
        req.setCourseId(courseId);
        List<AttendanceRecordDto> records = new ArrayList<>();
        for (int i = 0; i < presences.length; i++) {
            AttendanceRecordDto r = new AttendanceRecordDto();
            r.setMemberId(3000L + i);
            r.setMemberEmail("member" + i + "@test.com");
            r.setPresent(presences[i]);
            records.add(r);
        }
        req.setRecords(records);
        return req;
    }

    @Test
    void markAttendanceRecordsResults() {
        Long courseId = createPastCourse("2026-06-01T09:00");
        fixClock("2026-06-01T10:30"); // after course

        var results = attendanceService.markAttendance(attendanceReq(courseId, true, false), 8001L);
        assertThat(results).hasSize(2);
        assertThat(results.get(0).isPresent()).isTrue();
        assertThat(results.get(1).isPresent()).isFalse();
    }

    @Test
    void threeAbsencesSuspendsMember() {
        when(userClient.suspendMember(any(), any())).thenReturn(ApiResponse.success("Suspended"));

        Long courseId1 = createPastCourse("2026-06-01T09:00");
        Long courseId2 = createPastCourse("2026-06-02T09:00");
        Long courseId3 = createPastCourse("2026-06-03T09:00");

        fixClock("2026-06-04T08:00"); // after all courses

        attendanceService.markAttendance(mkReq(courseId1, 4001L, false), 8001L);
        attendanceService.markAttendance(mkReq(courseId2, 4001L, false), 8001L);
        attendanceService.markAttendance(mkReq(courseId3, 4001L, false), 8001L);

        verify(userClient).suspendMember(eq(4001L), eq("ADMIN"));

        // Verify MemberWarning event published 3 times (levels 1, 2, 3)
        verify(rabbitTemplate, times(3)).convertAndSend(
                eq("veltro.booking.exchange"),
                eq("member.warning"),
                any()
        );
    }

    private MarkAttendanceRequest mkReq(Long courseId, Long memberId, boolean present) {
        MarkAttendanceRequest req = new MarkAttendanceRequest();
        req.setCourseId(courseId);
        AttendanceRecordDto r = new AttendanceRecordDto();
        r.setMemberId(memberId);
        r.setMemberEmail("member@test.com");
        r.setPresent(present);
        req.setRecords(List.of(r));
        return req;
    }
}
