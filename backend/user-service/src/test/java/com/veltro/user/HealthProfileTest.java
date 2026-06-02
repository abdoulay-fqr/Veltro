package com.veltro.user;

import com.veltro.user.dto.CreateMemberRequest;
import com.veltro.user.dto.HealthProfileRequest;
import com.veltro.user.dto.HealthProfileResponse;
import com.veltro.user.dto.MemberResponse;
import com.veltro.user.service.HealthProfileService;
import com.veltro.user.service.MemberService;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class HealthProfileTest extends BaseIntegrationTest {

    @Autowired private MemberService memberService;
    @Autowired private HealthProfileService healthProfileService;

    @MockitoBean
    private RabbitTemplate rabbitTemplate;

    private MemberResponse createMember(long userId) {
        CreateMemberRequest req = new CreateMemberRequest();
        req.setUserId(userId);
        req.setIdentifier("hp" + userId + "@test.com");
        req.setFirstname("Test");
        req.setLastname("User");
        return memberService.create(req);
    }

    @Test
    void createAndRetrieveHealthProfile() {
        MemberResponse member = createMember(3001L);

        HealthProfileRequest req = new HealthProfileRequest();
        req.setFitnessObjective("WEIGHT_LOSS");
        req.setWeightKg(new BigDecimal("85.50"));
        req.setHeightCm(new BigDecimal("175.00"));
        req.setMedicalRestrictions("None");

        HealthProfileResponse created = healthProfileService.upsert(member.getId(), req);
        assertThat(created.getId()).isNotNull();
        assertThat(created.getFitnessObjective()).isEqualTo("WEIGHT_LOSS");
        assertThat(created.getWeightKg()).isEqualByComparingTo("85.50");

        HealthProfileResponse found = healthProfileService.findByMemberProfileId(member.getId());
        assertThat(found.getHeightCm()).isEqualByComparingTo("175.00");
    }

    @Test
    void upsertUpdatesExistingProfile() {
        MemberResponse member = createMember(3002L);

        HealthProfileRequest initial = new HealthProfileRequest();
        initial.setFitnessObjective("MUSCLE_GAIN");
        initial.setWeightKg(new BigDecimal("70.00"));
        healthProfileService.upsert(member.getId(), initial);

        HealthProfileRequest update = new HealthProfileRequest();
        update.setFitnessObjective("ENDURANCE");
        update.setWeightKg(new BigDecimal("68.50"));
        HealthProfileResponse updated = healthProfileService.upsert(member.getId(), update);

        assertThat(updated.getFitnessObjective()).isEqualTo("ENDURANCE");
        assertThat(updated.getWeightKg()).isEqualByComparingTo("68.50");
    }
}
