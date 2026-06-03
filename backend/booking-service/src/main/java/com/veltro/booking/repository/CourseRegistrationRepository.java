package com.veltro.booking.repository;

import com.veltro.booking.entity.CourseRegistration;
import com.veltro.booking.entity.RegistrationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRegistrationRepository extends JpaRepository<CourseRegistration, Long> {

    Optional<CourseRegistration> findByCourseIdAndMemberId(Long courseId, Long memberId);

    boolean existsByCourseIdAndMemberIdAndStatusNot(Long courseId, Long memberId, RegistrationStatus status);

    List<CourseRegistration> findByCourseIdAndStatusOrderByWaitlistPositionAsc(Long courseId, RegistrationStatus status);

    List<CourseRegistration> findByMemberIdAndStatusInOrderByRegisteredAtDesc(Long memberId, List<RegistrationStatus> statuses);

    List<CourseRegistration> findByCourseIdAndStatusIn(Long courseId, List<RegistrationStatus> statuses);

    Optional<CourseRegistration> findFirstByCourseIdAndStatusOrderByWaitlistPositionAsc(Long courseId, RegistrationStatus status);

    @Modifying
    @Query("UPDATE CourseRegistration r SET r.waitlistPosition = r.waitlistPosition - 1 WHERE r.courseId = :courseId AND r.status = 'WAITLISTED' AND r.waitlistPosition > :position")
    void shiftWaitlistPositionsDown(@Param("courseId") Long courseId, @Param("position") int position);

    @Query("SELECT COUNT(r) FROM CourseRegistration r WHERE r.courseId = :courseId AND r.status = 'WAITLISTED'")
    int countWaitlisted(@Param("courseId") Long courseId);
}
