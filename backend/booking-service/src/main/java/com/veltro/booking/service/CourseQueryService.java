package com.veltro.booking.service;

import com.veltro.booking.dto.CourseResponse;
import com.veltro.booking.entity.CourseLevel;
import com.veltro.booking.entity.CourseStatus;
import com.veltro.booking.exception.ResourceNotFoundException;
import com.veltro.booking.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CourseQueryService {

    private final CourseRepository courseRepo;

    @Transactional(readOnly = true)
    public Page<CourseResponse> findAll(LocalDateTime from, LocalDateTime to,
                                        CourseLevel level, Long coachId,
                                        CourseStatus status, Pageable pageable) {
        var spec = CourseSpecification.build(from, to, level, coachId, status);
        return courseRepo.findAll(spec, pageable).map(CourseResponse::from);
    }

    @Cacheable(value = "courseById", key = "#id")
    @Transactional(readOnly = true)
    public CourseResponse findById(Long id) {
        return CourseResponse.from(
                courseRepo.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Course not found: " + id))
        );
    }
}
