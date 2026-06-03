package com.veltro.booking;

import com.veltro.booking.dto.CreateCourseRequest;
import com.veltro.booking.dto.CourseResponse;
import com.veltro.booking.entity.CourseLevel;
import com.veltro.booking.entity.CourseStatus;
import com.veltro.booking.service.CourseCommandService;
import com.veltro.booking.service.CourseQueryService;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class CourseManagementTest extends BaseIntegrationTest {

    @Autowired private CourseCommandService commandService;
    @Autowired private CourseQueryService queryService;
    @MockitoBean private RabbitTemplate rabbitTemplate;
    @MockitoBean private Clock clock;

    private void fixClock(String dateTime) {
        when(clock.instant()).thenReturn(Instant.parse(dateTime + ":00Z"));
        when(clock.getZone()).thenReturn(ZoneOffset.UTC);
    }

    private CreateCourseRequest buildCourse(String dateTime) {
        CreateCourseRequest req = new CreateCourseRequest();
        req.setCoachId(1001L);
        req.setName("Morning Yoga");
        req.setDescription("Relaxing yoga session");
        req.setDateTime(LocalDateTime.parse(dateTime));
        req.setDurationMinutes(60);
        req.setCapacity(10);
        req.setLevel(CourseLevel.BEGINNER);
        req.setRoom("Studio A");
        return req;
    }

    @Test
    void createAndRetrieveCourse() {
        fixClock("2026-06-01T08:00");
        CourseResponse created = commandService.create(buildCourse("2026-06-05T09:00"));
        assertThat(created.getId()).isNotNull();
        assertThat(created.getStatus()).isEqualTo(CourseStatus.SCHEDULED);
        assertThat(created.getAvailableSpots()).isEqualTo(10);
        assertThat(created.getFillRate()).isEqualTo(0.0);

        CourseResponse found = queryService.findById(created.getId());
        assertThat(found.getName()).isEqualTo("Morning Yoga");
    }

    @Test
    void cancelCoursePublishesEvent() {
        fixClock("2026-06-01T08:00");
        CourseResponse created = commandService.create(buildCourse("2026-06-05T09:00"));

        commandService.cancel(created.getId(), 1001L);

        CourseResponse cancelled = queryService.findById(created.getId());
        assertThat(cancelled.getStatus()).isEqualTo(CourseStatus.CANCELLED);

        org.mockito.Mockito.verify(rabbitTemplate).convertAndSend(
                org.mockito.ArgumentMatchers.eq("veltro.booking.exchange"),
                org.mockito.ArgumentMatchers.eq("course.cancelled"),
                (Object) org.mockito.ArgumentMatchers.any()
        );
    }
}
