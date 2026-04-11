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
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberProfileRepository memberRepo;
    private final AvatarStorageService avatarStorage;
    private final RabbitTemplate rabbitTemplate;

    // ── CREATE ──────────────────────────────────────────────────────────────

    @Transactional
    public MemberResponse create(CreateMemberRequest req) {
        if (memberRepo.existsByUserId(req.getUserId())) {
            throw new ConflictException("Member profile already exists for userId: " + req.getUserId());
        }

        MemberProfile member = new MemberProfile();
        member.setUserId(req.getUserId());
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