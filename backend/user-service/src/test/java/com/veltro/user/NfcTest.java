package com.veltro.user;

import com.veltro.user.dto.CreateMemberRequest;
import com.veltro.user.dto.MemberResponse;
import com.veltro.user.dto.NfcActivateRequest;
import com.veltro.user.dto.NfcCardResponse;
import com.veltro.user.dto.SimulateScanResponse;
import com.veltro.user.entity.NfcCardStatus;
import com.veltro.user.event.NfcCardActivatedEvent;
import com.veltro.user.service.MemberService;
import com.veltro.user.service.NfcService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

class NfcTest extends BaseIntegrationTest {

    @Autowired private MemberService memberService;
    @Autowired private NfcService nfcService;

    @MockitoBean
    private RabbitTemplate rabbitTemplate;

    private MemberResponse createMember(long userId) {
        CreateMemberRequest req = new CreateMemberRequest();
        req.setUserId(userId);
        req.setIdentifier("nfc" + userId + "@test.com");
        req.setFirstname("NFC");
        req.setLastname("Tester");
        return memberService.create(req);
    }

    @Test
    void activateNfcCard() {
        MemberResponse member = createMember(4001L);
        NfcActivateRequest req = new NfcActivateRequest("card-test-001");

        NfcCardResponse card = nfcService.activate(member.getId(), req);
        assertThat(card.getCardUid()).isEqualTo("card-test-001");
        assertThat(card.getStatus()).isEqualTo(NfcCardStatus.ACTIVE);
    }

    @Test
    void deactivateNfcCard() {
        MemberResponse member = createMember(4002L);
        nfcService.activate(member.getId(), new NfcActivateRequest("card-test-002"));

        NfcCardResponse deactivated = nfcService.deactivate(member.getId(), "card-test-002");
        assertThat(deactivated.getStatus()).isEqualTo(NfcCardStatus.INACTIVE);
    }

    @Test
    void simulateScanReturnsCorrectMember() {
        MemberResponse member = createMember(4003L);
        nfcService.activate(member.getId(), new NfcActivateRequest("card-test-003"));

        SimulateScanResponse scan = nfcService.simulateScan("card-test-003");
        assertThat(scan.getCardUid()).isEqualTo("card-test-003");
        assertThat(scan.getFirstname()).isEqualTo("NFC");
        assertThat(scan.getMemberId()).isEqualTo(member.getId());
    }

    @Test
    void listNfcCardsForMember() {
        MemberResponse member = createMember(4004L);
        nfcService.activate(member.getId(), new NfcActivateRequest("card-test-004a"));
        nfcService.activate(member.getId(), new NfcActivateRequest("card-test-004b"));

        List<NfcCardResponse> cards = nfcService.getCardsForMember(member.getId());
        assertThat(cards).hasSize(2);
    }

    @Test
    void activatePublishesNfcCardActivatedEvent() {
        MemberResponse member = createMember(4005L);
        nfcService.activate(member.getId(), new NfcActivateRequest("card-test-005"));

        ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
        verify(rabbitTemplate).convertAndSend(
                eq("veltro.user.exchange"),
                eq("nfc.card.activated"),
                eventCaptor.capture()
        );

        NfcCardActivatedEvent event = (NfcCardActivatedEvent) eventCaptor.getValue();
        assertThat(event.getCardUid()).isEqualTo("card-test-005");
        assertThat(event.getMemberProfileId()).isEqualTo(member.getId());
    }
}
