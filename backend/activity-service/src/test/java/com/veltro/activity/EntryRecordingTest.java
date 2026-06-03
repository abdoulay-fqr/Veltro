package com.veltro.activity;

import com.veltro.activity.client.NfcClient;
import com.veltro.activity.dto.EntryRequest;
import com.veltro.activity.dto.EntryResponse;
import com.veltro.activity.entity.Direction;
import com.veltro.activity.exception.BusinessRuleException;
import com.veltro.activity.exception.ServiceUnavailableException;
import com.veltro.activity.repository.GymEntryRepository;
import com.veltro.activity.service.EntryCommandService;
import com.veltro.common.dto.ApiResponse;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class EntryRecordingTest extends BaseIntegrationTest {

    @Autowired private EntryCommandService entryCmd;
    @Autowired private GymEntryRepository entryRepo;

    @MockitoBean private NfcClient nfcClient;
    @MockitoBean private RabbitTemplate rabbitTemplate;
    @MockitoBean private Clock clock;

    private void fixClock() {
        when(clock.instant()).thenReturn(Instant.parse("2026-06-01T09:00:00Z"));
        when(clock.getZone()).thenReturn(ZoneOffset.UTC);
    }

    private void mockActiveCard(String cardUid, Long memberId) {
        NfcClient.NfcScanResult result = new NfcClient.NfcScanResult(
                cardUid, "ACTIVE", memberId, memberId, "Alice", "Smith",
                "+1-555-1234", null, "ACTIVE");
        when(nfcClient.simulateScan(any())).thenReturn(ApiResponse.success(result));
    }

    private void mockInactiveCard(String cardUid) {
        NfcClient.NfcScanResult result = new NfcClient.NfcScanResult(
                cardUid, "INACTIVE", 999L, 999L, "Bob", "Jones",
                null, null, "ACTIVE");
        when(nfcClient.simulateScan(any())).thenReturn(ApiResponse.success(result));
    }

    @Test
    void entryWithActiveCardSavesAndPublishesEvent() {
        fixClock();
        mockActiveCard("card001", 1001L);

        EntryRequest req = new EntryRequest();
        req.setCardUid("card001");
        req.setDirection(Direction.IN);

        EntryResponse resp = entryCmd.recordEntry(req);
        assertThat(resp.getMemberId()).isEqualTo(1001L);
        assertThat(resp.getMemberName()).isEqualTo("Alice Smith");
        assertThat(resp.getDirection()).isEqualTo(Direction.IN);
        assertThat(resp.getSessionId()).isNotNull();

        verify(rabbitTemplate).convertAndSend(
                eq("veltro.activity.exchange"),
                eq("activity.entry.recorded"),
                any()
        );
    }

    @Test
    void entryWithDeactivatedCardRejected() {
        fixClock();
        mockInactiveCard("card002");

        EntryRequest req = new EntryRequest();
        req.setCardUid("card002");
        req.setDirection(Direction.IN);

        assertThatThrownBy(() -> entryCmd.recordEntry(req))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("deactivated");
    }

    @Test
    void entryWithUserServiceDownThrowsServiceUnavailable() {
        fixClock();
        when(nfcClient.simulateScan(any()))
                .thenThrow(new ServiceUnavailableException("Card validation unavailable — user service is down"));

        EntryRequest req = new EntryRequest();
        req.setCardUid("card003");
        req.setDirection(Direction.IN);

        assertThatThrownBy(() -> entryCmd.recordEntry(req))
                .isInstanceOf(ServiceUnavailableException.class)
                .hasMessageContaining("unavailable");
    }

    @Test
    void inOutPairingSharesSessionId() {
        fixClock();
        mockActiveCard("card004", 1002L);

        EntryRequest inReq = new EntryRequest();
        inReq.setCardUid("card004");
        inReq.setDirection(Direction.IN);

        EntryRequest outReq = new EntryRequest();
        outReq.setCardUid("card004");
        outReq.setDirection(Direction.OUT);

        EntryResponse inResp = entryCmd.recordEntry(inReq);
        EntryResponse outResp = entryCmd.recordEntry(outReq);

        assertThat(inResp.getSessionId()).isNotNull();
        assertThat(outResp.getSessionId()).isEqualTo(inResp.getSessionId());
    }
}
