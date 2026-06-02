package com.veltro.user;

import com.veltro.user.dto.CoachResponse;
import com.veltro.user.dto.CreateCoachRequest;
import com.veltro.user.dto.UpdateCoachRequest;
import com.veltro.user.entity.AccountStatus;
import com.veltro.user.service.CoachService;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.assertj.core.api.Assertions.assertThat;

class CoachCrudTest extends BaseIntegrationTest {

    @Autowired
    private CoachService coachService;

    @MockitoBean
    private RabbitTemplate rabbitTemplate;

    private CreateCoachRequest buildRequest(long userId) {
        CreateCoachRequest req = new CreateCoachRequest();
        req.setUserId(userId);
        req.setIdentifier("coach" + userId + "@test.com");
        req.setFirstname("Bob");
        req.setLastname("Johnson");
        req.setPhone("+1-555-200" + userId);
        req.setBio("Certified personal trainer");
        req.setSpecialization("Strength & Conditioning");
        req.setCertifications("NASM-CPT");
        return req;
    }

    @Test
    void createAndReadCoach() {
        CoachResponse created = coachService.create(buildRequest(2001L));
        assertThat(created.getId()).isNotNull();
        assertThat(created.getFirstname()).isEqualTo("Bob");
        assertThat(created.getBio()).isEqualTo("Certified personal trainer");
        assertThat(created.getStatus()).isEqualTo(AccountStatus.ACTIVE);

        CoachResponse found = coachService.findById(created.getId());
        assertThat(found.getSpecialization()).isEqualTo("Strength & Conditioning");
    }

    @Test
    void updateCoach() {
        CoachResponse created = coachService.create(buildRequest(2002L));

        UpdateCoachRequest update = new UpdateCoachRequest();
        update.setBio("Updated bio");
        update.setSpecialization("Yoga");

        CoachResponse updated = coachService.update(created.getId(), update);
        assertThat(updated.getBio()).isEqualTo("Updated bio");
        assertThat(updated.getSpecialization()).isEqualTo("Yoga");
        assertThat(updated.getFirstname()).isEqualTo("Bob");
    }

    @Test
    void suspendAndActivateCoach() {
        CoachResponse created = coachService.create(buildRequest(2003L));

        CoachResponse suspended = coachService.suspend(created.getId());
        assertThat(suspended.getStatus()).isEqualTo(AccountStatus.SUSPENDED);

        CoachResponse reactivated = coachService.activate(created.getId());
        assertThat(reactivated.getStatus()).isEqualTo(AccountStatus.ACTIVE);
    }

    @Test
    void findByUserIdReturnsCorrectCoach() {
        CoachResponse created = coachService.create(buildRequest(2004L));
        CoachResponse found = coachService.findByUserId(2004L);
        assertThat(found.getId()).isEqualTo(created.getId());
        assertThat(found.getFirstname()).isEqualTo("Bob");
    }
}
