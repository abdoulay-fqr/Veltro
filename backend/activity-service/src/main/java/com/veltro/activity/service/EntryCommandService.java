package com.veltro.activity.service;

import com.veltro.activity.client.NfcClient;
import com.veltro.activity.config.RabbitMQConfig;
import com.veltro.activity.dto.EntryRequest;
import com.veltro.activity.dto.EntryResponse;
import com.veltro.activity.entity.Direction;
import com.veltro.activity.entity.GymEntry;
import com.veltro.activity.event.GymEntryRecordedEvent;
import com.veltro.activity.exception.BusinessRuleException;
import com.veltro.activity.repository.GymEntryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class EntryCommandService {

    private final GymEntryRepository entryRepo;
    private final NfcClient nfcClient;
    private final RabbitTemplate rabbitTemplate;
    private final Clock clock;

    @Transactional
    public EntryResponse recordEntry(EntryRequest req) {
        // 1. Validate card via user-service
        var scanResult = nfcClient.simulateScan(Map.of("cardUid", req.getCardUid()));

        if (scanResult == null || scanResult.getData() == null) {
            throw new BusinessRuleException("Card not found: " + req.getCardUid());
        }

        NfcClient.NfcScanResult nfc = scanResult.getData();

        if (!"ACTIVE".equalsIgnoreCase(nfc.cardStatus())) {
            throw new BusinessRuleException("Card is deactivated. Please contact gym staff.");
        }
        if ("SUSPENDED".equalsIgnoreCase(nfc.accountStatus())) {
            throw new BusinessRuleException("Member account is suspended.");
        }

        // 2. Pair IN/OUT by sessionId
        String sessionId;
        if (req.getDirection() == Direction.IN) {
            sessionId = UUID.randomUUID().toString();
        } else {
            // Find last unmatched IN for this cardUid
            List<GymEntry> unmatchedIn = entryRepo.findUnmatchedInEntries(req.getCardUid(), PageRequest.of(0, 1));
            sessionId = unmatchedIn.isEmpty() ? UUID.randomUUID().toString() : unmatchedIn.get(0).getSessionId();
        }

        // 3. Save entry
        GymEntry entry = new GymEntry();
        entry.setMemberId(nfc.memberId());
        entry.setCardUid(req.getCardUid());
        entry.setMemberName(nfc.firstname() + " " + nfc.lastname());
        entry.setDirection(req.getDirection());
        entry.setTimestamp(LocalDateTime.now(clock));
        entry.setSessionId(sessionId);
        entryRepo.save(entry);

        // 4. Publish event
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.ACTIVITY_EXCHANGE,
                RabbitMQConfig.GYM_ENTRY_RECORDED_ROUTING_KEY,
                new GymEntryRecordedEvent(entry.getId(), entry.getMemberId(), entry.getMemberName(),
                        entry.getCardUid(), entry.getDirection(), entry.getTimestamp(), entry.getSessionId()));

        log.info("GymEntry recorded: cardUid={}, direction={}, memberId={}", req.getCardUid(), req.getDirection(), nfc.memberId());
        return EntryResponse.from(entry);
    }
}
