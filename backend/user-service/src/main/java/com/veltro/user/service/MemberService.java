package com.veltro.user.service;

import com.veltro.user.config.RabbitMQConfig;
import com.veltro.user.dto.CreateMemberRequest;
import com.veltro.user.dto.MemberResponse;
import com.veltro.user.dto.UpdateMemberRequest;
import com.veltro.user.entity.AccountStatus;
import com.veltro.user.entity.MemberProfile;
import com.veltro.user.event.UserCreatedEvent;
import com.veltro.user.exception.ConflictException;
import com.veltro.user.exception.ResourceNotFoundException;
import com.veltro.user.repository.MemberProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemberService {

    private final MemberProfileRepository memberRepo;
    private final AvatarStorageService avatarStorage;
    private final RabbitTemplate rabbitTemplate;
    private final RestTemplate restTemplate;

    @Value("${AUTH_SERVICE_BASE_URL:http://localhost:8081}")
    private String authServiceBaseUrl;

    // ── CREATE ──────────────────────────────────────────────────────────────

    @Transactional
    public MemberResponse create(CreateMemberRequest req) {
        if (memberRepo.existsByUserId(req.getUserId())) {
            throw new ConflictException("Member profile already exists for userId: " + req.getUserId());
        }

        MemberProfile member = new MemberProfile();
        member.setUserId(req.getUserId());
        member.setEmail(req.getIdentifier());
        member.setFirstname(req.getFirstname());
        member.setLastname(req.getLastname());
        member.setPhone(req.getPhone());
        member.setDateOfBirth(req.getDateOfBirth());

        memberRepo.save(member);

        // Publish UserCreated event → subscription-service will auto-assign TRIAL
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.USER_EXCHANGE,
                RabbitMQConfig.USER_CREATED_ROUTING_KEY,
                new UserCreatedEvent(req.getUserId(), "MEMBER", req.getIdentifier())
        );

        return MemberResponse.from(member);
    }

    // ── READ ─────────────────────────────────────────────────────────────────

    public Page<MemberResponse> findAll(Pageable pageable) {
        return memberRepo.findAll(pageable).map(MemberResponse::from);
    }

    public Page<MemberResponse> findByStatus(AccountStatus status, Pageable pageable) {
        return memberRepo.findByStatus(status, pageable).map(MemberResponse::from);
    }

    public MemberResponse findById(Long id) {
        return MemberResponse.from(getOrThrow(id));
    }

    public MemberResponse findByUserId(Long userId) {
        return MemberResponse.from(
                memberRepo.findByUserId(userId)
                        .orElseThrow(() -> new ResourceNotFoundException("Member not found for userId: " + userId))
        );
    }

    // ── UPDATE ───────────────────────────────────────────────────────────────

    @Transactional
    public MemberResponse update(Long id, UpdateMemberRequest req) {
        MemberProfile member = getOrThrow(id);

        if (req.getFirstname() != null)   member.setFirstname(req.getFirstname());
        if (req.getLastname() != null)    member.setLastname(req.getLastname());
        if (req.getPhone() != null)       member.setPhone(req.getPhone());
        if (req.getDateOfBirth() != null) member.setDateOfBirth(req.getDateOfBirth());

        return MemberResponse.from(memberRepo.save(member));
    }

    // ── AVATAR ───────────────────────────────────────────────────────────────

    @Transactional
    public MemberResponse uploadAvatar(Long id, MultipartFile file) {
        MemberProfile member = getOrThrow(id);
        String url = avatarStorage.store(file, "member_" + id);
        member.setAvatarUrl(url);
        return MemberResponse.from(memberRepo.save(member));
    }

    // ── DELETE ───────────────────────────────────────────────────────────────

    @Transactional
    public void delete(Long id) {
        MemberProfile member = getOrThrow(id);
        Long userId = member.getUserId();

        // nfc_card and health_profile both have ON DELETE CASCADE, so
        // deleting the member_profile row cascades them automatically.
        memberRepo.delete(member);

        // Also remove the auth-service account so the user cannot log in again.
        // Fire-and-forget: if auth-service is down the member profile is already
        // gone, which is the important part.
        try {
            restTemplate.delete(authServiceBaseUrl + "/api/v1/auth/users/" + userId);
        } catch (Exception e) {
            log.warn("Member profile {} deleted but auth user {} could not be removed: {}",
                    id, userId, e.getMessage());
        }
    }

    // ── SUSPEND / ACTIVATE ───────────────────────────────────────────────────

    @Transactional
    public MemberResponse suspend(Long id) {
        MemberProfile member = getOrThrow(id);
        member.setStatus(AccountStatus.SUSPENDED);
        return MemberResponse.from(memberRepo.save(member));
    }

    @Transactional
    public MemberResponse activate(Long id) {
        MemberProfile member = getOrThrow(id);
        member.setStatus(AccountStatus.ACTIVE);
        return MemberResponse.from(memberRepo.save(member));
    }

    // ── HELPER ───────────────────────────────────────────────────────────────

    private MemberProfile getOrThrow(Long id) {
        return memberRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found with id: " + id));
    }
}