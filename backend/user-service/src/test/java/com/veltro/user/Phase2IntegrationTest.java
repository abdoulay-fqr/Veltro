package com.veltro.user;

import com.veltro.user.dto.*;
import com.veltro.user.entity.AccountStatus;
import com.veltro.user.entity.NfcCardStatus;
import com.veltro.user.service.MemberService;
import com.veltro.user.service.NfcService;
import com.veltro.user.service.HealthProfileService;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Phase 2 end-to-end integration test.
 *
 * Flow: create member → assign NFC card → update profile → check NFC status
 *
 * Tag commit: user-complete
 */
class Phase2IntegrationTest extends BaseIntegrationTest {

    @Autowired private MemberService memberService;
    @Autowired private NfcService nfcService;
    @Autowired private HealthProfileService healthProfileService;

    @MockitoBean
    private RabbitTemplate rabbitTemplate;

    @Test
    void fullMemberLifecycleWithNfcAndHealth() {
        // Step 1: Create member (simulates web admin creating member after registration)
        CreateMemberRequest createReq = new CreateMemberRequest();
        createReq.setUserId(9001L);
        createReq.setIdentifier("e2e-member@veltro.com");
        createReq.setFirstname("E2E");
        createReq.setLastname("Member");
        createReq.setPhone("+1-555-9001");

        MemberResponse member = memberService.create(createReq);
        assertThat(member.getId()).isNotNull();
        assertThat(member.getStatus()).isEqualTo(AccountStatus.ACTIVE);

        // Step 2: Assign NFC card (admin action)
        NfcActivateRequest nfcReq = new NfcActivateRequest("e2e-card-001");
        NfcCardResponse card = nfcService.activate(member.getId(), nfcReq);
        assertThat(card.getCardUid()).isEqualTo("e2e-card-001");
        assertThat(card.getStatus()).isEqualTo(NfcCardStatus.ACTIVE);

        // Step 3: Simulate mobile login — member updates their profile
        UpdateMemberRequest updateReq = new UpdateMemberRequest();
        updateReq.setPhone("+1-555-9002");
        MemberResponse updated = memberService.update(member.getId(), updateReq);
        assertThat(updated.getPhone()).isEqualTo("+1-555-9002");

        // Step 4: Set health profile (via mobile profile screen)
        HealthProfileRequest healthReq = new HealthProfileRequest();
        healthReq.setFitnessObjective("WEIGHT_LOSS");
        healthReq.setWeightKg(new BigDecimal("80.00"));
        healthReq.setHeightCm(new BigDecimal("175.00"));
        HealthProfileResponse health = healthProfileService.upsert(member.getId(), healthReq);
        assertThat(health.getFitnessObjective()).isEqualTo("WEIGHT_LOSS");

        // Step 5: Check NFC card status (simulate-scan call from NFC reader / NFC simulator panel)
        SimulateScanResponse scan = nfcService.simulateScan("e2e-card-001");
        assertThat(scan.getCardStatus()).isEqualTo(NfcCardStatus.ACTIVE);
        assertThat(scan.getAccountStatus()).isEqualTo("ACTIVE");
        assertThat(scan.getFirstname()).isEqualTo("E2E");
        assertThat(scan.getPhone()).isEqualTo("+1-555-9002");

        // Step 6: Admin suspends member — NFC scan still returns data (caller decides)
        memberService.suspend(member.getId());
        SimulateScanResponse scanAfterSuspend = nfcService.simulateScan("e2e-card-001");
        assertThat(scanAfterSuspend.getAccountStatus()).isEqualTo("SUSPENDED");
        assertThat(scanAfterSuspend.getCardStatus()).isEqualTo(NfcCardStatus.ACTIVE);

        // Step 7: Deactivate NFC card
        NfcCardResponse deactivated = nfcService.deactivate(member.getId(), "e2e-card-001");
        assertThat(deactivated.getStatus()).isEqualTo(NfcCardStatus.INACTIVE);
    }
}
