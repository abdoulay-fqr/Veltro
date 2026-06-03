package com.veltro.booking.service;

import com.veltro.booking.entity.Course;
import com.veltro.booking.entity.CourseLevel;
import com.veltro.booking.entity.CourseStatus;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

public class CourseSpecification {

    public static Specification<Course> dateFrom(LocalDateTime from) {
        return (root, query, cb) -> from == null ? null : cb.greaterThanOrEqualTo(root.get("dateTime"), from);
    }

    public static Specification<Course> dateTo(LocalDateTime to) {
        return (root, query, cb) -> to == null ? null : cb.lessThanOrEqualTo(root.get("dateTime"), to);
    }

    public static Specification<Course> hasLevel(CourseLevel level) {
        return (root, query, cb) -> level == null ? null : cb.equal(root.get("level"), level);
    }

    public static Specification<Course> hasCoachId(Long coachId) {
        return (root, query, cb) -> coachId == null ? null : cb.equal(root.get("coachId"), coachId);
    }

    public static Specification<Course> hasStatus(CourseStatus status) {
        return (root, query, cb) -> status == null ? null : cb.equal(root.get("status"), status);
    }

    public static Specification<Course> build(
            LocalDateTime from, LocalDateTime to, CourseLevel level, Long coachId, CourseStatus status) {
        return Specification.where(dateFrom(from))
                .and(dateTo(to))
                .and(hasLevel(level))
                .and(hasCoachId(coachId))
                .and(hasStatus(status));
    }
}
