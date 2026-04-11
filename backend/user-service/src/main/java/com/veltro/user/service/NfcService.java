package com.veltro.user.service;

import com.veltro.user.dto.NfcActivateRequest;
import com.veltro.user.dto.NfcCardResponse;
import com.veltro.user.dto.SimulateScanResponse;
import com.veltro.user.entity.NfcCard;
import com.veltro.user.entity.NfcCardStatus;
import com.veltro.user.exception.ConflictException;
import com.veltro.user.exception.ResourceNotFoundException;
import com.veltro.user.repository.MemberProfileRepository;
import com.veltro.user.repository.NfcCardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NfcService {

    private final NfcCardRepository nfcCardRepository;
    private final MemberProfileRepository memberProfileRepository;

    // ── Activate: assign a new cardUid to a member ──────────────────────────
    @Transactional
    public NfcCardResponse activate(Long memberProfileId, NfcActivateRequest request) {
        // Member must exist
        memberProfileRepository.findById(memberProfileId)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found: " + memberProfileId));

        // cardUid must be globally unique
        nfcCardRepository.findByCardUid(request.getCardUid()).ifPresent(existing -> {
            throw new ConflictException("Card UID already in use: " + request.getCardUid());
        });

        NfcCard card = new NfcCard();
        card.setMemberProfile(memberProfileRepository.getReferenceById(memberProfileId));
        card.setCardUid(request.getCardUid());
        card.setStatus(NfcCardStatus.ACTIVE);

        return new NfcCardResponse(nfcCardRepository.save(card));
    }

    // ── Deactivate: mark card INACTIVE by cardUid ────────────────────────────
    @Transactional
    public NfcCardResponse deactivate(Long memberProfileId, String cardUid) {
        NfcCard card = nfcCardRepository.findByCardUid(cardUid)
                .orElseThrow(() -> new ResourceNotFoundException("NFC card not found: " + cardUid));

        if (!card.getMemberProfile().getId().equals(memberProfileId)) {
            throw new com.veltro.user.exception.AccessDeniedException("Card does not belong to this member");
        }

        card.setStatus(NfcCardStatus.INACTIVE);
        return new NfcCardResponse(nfcCardRepository.save(card));
    }

    // ── Simulate scan: Node-RED calls this with a cardUid ────────────────────
    @Transactional(readOnly = true)
    public SimulateScanResponse simulateScan(String cardUid) {
        NfcCard card = nfcCardRepository.findByCardUid(cardUid)
                .orElseThrow(() -> new ResourceNotFoundException("NFC card not found: " + cardUid));
        // Return member info regardless of status — caller decides what to do with INACTIVE cards
        return new SimulateScanResponse(card);
    }

    // ── List all cards for a member ──────────────────────────────────────────
    public List<NfcCardResponse> getCardsForMember(Long memberProfileId) {
        memberProfileRepository.findById(memberProfileId)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found: " + memberProfileId));
        return nfcCardRepository.findByMemberProfileId(memberProfileId)
                .stream().map(NfcCardResponse::new).toList();
    }
}