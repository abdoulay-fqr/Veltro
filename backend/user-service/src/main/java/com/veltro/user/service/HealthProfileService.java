package com.veltro.user.service;

import com.veltro.user.dto.HealthProfileRequest;
import com.veltro.user.dto.HealthProfileResponse;
import com.veltro.user.entity.HealthProfile;
import com.veltro.user.exception.ConflictException;
import com.veltro.user.exception.ResourceNotFoundException;
import com.veltro.user.repository.HealthProfileRepository;
import com.veltro.user.repository.MemberProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class HealthProfileService {

    private final HealthProfileRepository healthProfileRepo;
    private final MemberProfileRepository memberProfileRepo;

    @Transactional
    public HealthProfileResponse upsert(Long memberProfileId, HealthProfileRequest req) {
        var member = memberProfileRepo.findById(memberProfileId)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found: " + memberProfileId));

        HealthProfile hp = healthProfileRepo.findByMemberProfileId(memberProfileId)
                .orElseGet(() -> {
                    HealthProfile h = new HealthProfile();
                    h.setMemberProfile(member);
                    return h;
                });

        if (req.getFitnessObjective() != null)    hp.setFitnessObjective(req.getFitnessObjective());
        if (req.getMedicalRestrictions() != null) hp.setMedicalRestrictions(req.getMedicalRestrictions());
        if (req.getWeightKg() != null)            hp.setWeightKg(req.getWeightKg());
        if (req.getHeightCm() != null)            hp.setHeightCm(req.getHeightCm());

        return HealthProfileResponse.from(healthProfileRepo.save(hp));
    }

    @Transactional(readOnly = true)
    public HealthProfileResponse findByMemberProfileId(Long memberProfileId) {
        return HealthProfileResponse.from(
                healthProfileRepo.findByMemberProfileId(memberProfileId)
                        .orElseThrow(() -> new ResourceNotFoundException("Health profile not found for member: " + memberProfileId))
        );
    }
}
