package com.veltro.booking;

import com.veltro.booking.client.SubscriptionClient;
import com.veltro.booking.client.UserClient;
import com.veltro.booking.dto.BookingRequest;
import com.veltro.booking.dto.BookingResponse;
import com.veltro.booking.dto.CreateCourseRequest;
import com.veltro.booking.dto.CourseResponse;
import com.veltro.booking.entity.CourseLevel;
import com.veltro.booking.entity.RegistrationStatus;
import com.veltro.booking.exception.BusinessRuleException;
import com.veltro.booking.exception.ServiceUnavailableException;
import com.veltro.booking.service.BookingCommandService;
import com.veltro.booking.service.BookingQueryService;
import com.veltro.booking.service.CourseCommandService;
import com.veltro.common.dto.ApiResponse;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class BookingLifecycleTest extends BaseIntegrationTest {

    @Autowired private BookingCommandService bookingCmd;
    @Autowired private BookingQueryService bookingQuery;
    @Autowired private CourseCommandService courseCmd;

    @MockitoBean private SubscriptionClient subscriptionClient;
    @MockitoBean private UserClient userClient;
    @MockitoBean private RabbitTemplate rabbitTemplate;
    @MockitoBean private Clock clock;

    private void fixClock(String dateTime) {
        when(clock.instant()).thenReturn(Instant.parse(dateTime + ":00Z"));
        when(clock.getZone()).thenReturn(ZoneOffset.UTC);
    }

    private void mockActiveSubscription(Long memberId) {
        ApiResponse<Map<String, Object>> resp = ApiResponse.success(Map.of("status", "ACTIVE", "plan", "MONTHLY"));
        when(subscriptionClient.getActiveSubscription(memberId)).thenReturn(resp);
    }

    private Long createCourse(int capacity, String dateTime) {
        fixClock("2026-06-01T08:00");
        CreateCourseRequest req = new CreateCourseRequest();
        req.setCoachId(9001L);
        req.setName("Test Course");
        req.setDateTime(LocalDateTime.parse(dateTime));
        req.setDurationMinutes(60);
        req.setCapacity(capacity);
        req.setLevel(CourseLevel.BEGINNER);
        return courseCmd.create(req).getId();
    }

    private BookingRequest bookReq(Long courseId) {
        BookingRequest req = new BookingRequest();
        req.setCourseId(courseId);
        req.setMemberEmail("member@test.com");
        return req;
    }

    @Test
    void successfulBooking() {
        Long courseId = createCourse(10, "2026-06-05T09:00");
        fixClock("2026-06-01T08:00");
        mockActiveSubscription(2001L);

        BookingResponse booking = bookingCmd.book(bookReq(courseId), 2001L);
        assertThat(booking.getStatus()).isEqualTo(RegistrationStatus.BOOKED);
    }

    @Test
    void bookingWindowEnforced() {
        Long courseId = createCourse(10, "2026-06-15T09:00"); // 14 days away
        fixClock("2026-06-01T08:00");
        mockActiveSubscription(2002L);

        assertThatThrownBy(() -> bookingCmd.book(bookReq(courseId), 2002L))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("7 days");
    }

    @Test
    void expiredSubscriptionRejected() {
        Long courseId = createCourse(10, "2026-06-05T09:00");
        fixClock("2026-06-01T08:00");

        ApiResponse<Map<String, Object>> resp = ApiResponse.success(Map.of("status", "EXPIRED", "plan", "MONTHLY"));
        when(subscriptionClient.getActiveSubscription(2003L)).thenReturn(resp);

        assertThatThrownBy(() -> bookingCmd.book(bookReq(courseId), 2003L))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("EXPIRED");
    }

    @Test
    void waitlistWhenFull() {
        Long courseId = createCourse(1, "2026-06-05T09:00");
        fixClock("2026-06-01T08:00");
        mockActiveSubscription(2004L);
        mockActiveSubscription(2005L);

        BookingResponse first = bookingCmd.book(bookReq(courseId), 2004L);
        assertThat(first.getStatus()).isEqualTo(RegistrationStatus.BOOKED);

        BookingResponse second = bookingCmd.book(bookReq(courseId), 2005L);
        assertThat(second.getStatus()).isEqualTo(RegistrationStatus.WAITLISTED);
        assertThat(second.getWaitlistPosition()).isEqualTo(1);
    }

    @Test
    void waitlistPromotion() {
        Long courseId = createCourse(1, "2026-06-05T09:00");
        fixClock("2026-06-01T08:00");
        mockActiveSubscription(2006L);
        mockActiveSubscription(2007L);

        BookingResponse bookedMember = bookingCmd.book(bookReq(courseId), 2006L);
        bookingCmd.book(bookReq(courseId), 2007L); // waitlisted

        // Cancel the booked member
        bookingCmd.cancel(bookedMember.getId(), 2006L);

        // Verify WaitlistPromoted event published
        verify(rabbitTemplate).convertAndSend(
                eq("veltro.booking.exchange"),
                eq("waitlist.promoted"),
                (Object) any()
        );

        // Verify waitlisted member is now BOOKED
        var bookings = bookingQuery.getMemberBookings(2007L, RegistrationStatus.BOOKED);
        assertThat(bookings).hasSize(1);
    }

    @Test
    void twoHourCancellationWindowEnforced() {
        Long courseId = createCourse(10, "2026-06-05T09:00");
        fixClock("2026-06-01T08:00");
        mockActiveSubscription(2008L);

        BookingResponse booking = bookingCmd.book(bookReq(courseId), 2008L);

        // Move clock to 1 hour before course (within 2-hour cancellation window)
        fixClock("2026-06-05T08:00"); // 1h before

        assertThatThrownBy(() -> bookingCmd.cancel(booking.getId(), 2008L))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("2 hours");
    }

    @Test
    void circuitBreakerFallbackThrowsServiceUnavailable() {
        Long courseId = createCourse(10, "2026-06-05T09:00");
        fixClock("2026-06-01T08:00");

        when(subscriptionClient.getActiveSubscription(2009L))
                .thenThrow(new ServiceUnavailableException("Subscription service is temporarily unavailable"));

        assertThatThrownBy(() -> bookingCmd.book(bookReq(courseId), 2009L))
                .isInstanceOf(ServiceUnavailableException.class);
    }
}
