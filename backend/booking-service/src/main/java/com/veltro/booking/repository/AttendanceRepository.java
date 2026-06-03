package com.veltro.booking.repository;

import com.veltro.booking.entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    List<Attendance> findByCourseId(Long courseId);

    long countByMemberIdAndPresentFalseAndProcessedForSuspensionFalse(Long memberId);

    List<Attendance> findByMemberIdAndPresentFalseAndProcessedForSuspensionFalse(Long memberId);

    boolean existsByCourseIdAndMemberId(Long courseId, Long memberId);
}
