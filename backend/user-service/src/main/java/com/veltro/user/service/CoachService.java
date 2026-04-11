package com.veltro.user.service;

import com.veltro.user.config.RabbitMQConfig;
import com.veltro.user.dto.CoachResponse;
import com.veltro.user.dto.CreateCoachRequest;
import com.veltro.user.dto.UpdateCoachRequest;
import com.veltro.user.entity.AccountStatus;
import com.veltro.user.entity.CoachProfile;
import com.veltro.user.event.UserCreatedEvent;
import com.veltro.user.exception.ConflictException;
import com.veltro.user.exception.ResourceNotFoundException;
import com.veltro.user.repository.CoachProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class CoachService {

    private final CoachProfileRepository coachRepo;
    private final AvatarStorageService avatarStorage;
    private final RabbitTemplate rabbitTemplate;

    // ── CREATE ──────────────────────────────────────────────────────────────

    @Transactional
    public CoachResponse create(CreateCoachRequest req) {
        if (coachRepo.existsByUserId(req.getUserId())) {
            throw new ConflictException("Coach profile already exists for userId: " + req.getUserId());
        }

        CoachProfile coach = new CoachProfile();
        coach.setUserId(req.getUserId());
        coach.setFirstname(req.getFirstname());
        coach.setLastname(req.getLastname());
        coach.setPhone(req.getPhone());
        coach.setDateOfBirth(req.getDateOfBirth());
        coach.setBio(req.getBio());
        coach.setSpecialization(req.getSpecialization());
        coach.setCertifications(req.getCertifications());

        coachRepo.save(coach);

        // Publish UserCreated event
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.USER_EXCHANGE,
                RabbitMQConfig.USER_CREATED_ROUTING_KEY,
                new UserCreatedEvent(req.getUserId(), "COACH", req.getIdentifier())
        );

        return CoachResponse.from(coach);
    }

    // ── READ ─────────────────────────────────────────────────────────────────

    public Page<CoachResponse> findAll(Pageable pageable) {
        return coachRepo.findAll(pageable).map(CoachResponse::from);
    }

    public Page<CoachResponse> findByStatus(AccountStatus status, Pageable pageable) {
        return coachRepo.findByStatus(status, pageable).map(CoachResponse::from);
    }

    public CoachResponse findById(Long id) {
        return CoachResponse.from(getOrThrow(id));
    }

    public CoachResponse findByUserId(Long userId) {
        return CoachResponse.from(
                coachRepo.findByUserId(userId)
                        .orElseThrow(() -> new ResourceNotFoundException("Coach not found for userId: " + userId))
        );
    }

    // ── UPDATE ───────────────────────────────────────────────────────────────

    @Transactional
    public CoachResponse update(Long id, UpdateCoachRequest req) {
        CoachProfile coach = getOrThrow(id);

        if (req.getFirstname() != null)      coach.setFirstname(req.getFirstname());
        if (req.getLastname() != null)       coach.setLastname(req.getLastname());
        if (req.getPhone() != null)          coach.setPhone(req.getPhone());
        if (req.getDateOfBirth() != null)    coach.setDateOfBirth(req.getDateOfBirth());
        if (req.getBio() != null)            coach.setBio(req.getBio());
        if (req.getSpecialization() != null) coach.setSpecialization(req.getSpecialization());
        if (req.getCertifications() != null) coach.setCertifications(req.getCertifications());

        return CoachResponse.from(coachRepo.save(coach));
    }

    // ── AVATAR ───────────────────────────────────────────────────────────────

    @Transactional
    public CoachResponse uploadAvatar(Long id, MultipartFile file) {
        CoachProfile coach = getOrThrow(id);
        String url = avatarStorage.store(file, "coach_" + id);
        coach.setAvatarUrl(url);
        return CoachResponse.from(coachRepo.save(coach));
    }

    // ── SUSPEND / ACTIVATE ───────────────────────────────────────────────────

    @Transactional
    public CoachResponse suspend(Long id) {
        CoachProfile coach = getOrThrow(id);
        coach.setStatus(AccountStatus.SUSPENDED);
        return CoachResponse.from(coachRepo.save(coach));
    }

    @Transactional
    public CoachResponse activate(Long id) {
        CoachProfile coach = getOrThrow(id);
        coach.setStatus(AccountStatus.ACTIVE);
        return CoachResponse.from(coachRepo.save(coach));
    }

    // ── HELPER ───────────────────────────────────────────────────────────────

    private CoachProfile getOrThrow(Long id) {
        return coachRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Coach not found with id: " + id));
    }
}