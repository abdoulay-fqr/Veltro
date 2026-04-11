package com.veltro.user.repository;

import com.veltro.user.entity.NfcCard;
import com.veltro.user.entity.NfcCardStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NfcCardRepository extends JpaRepository<NfcCard, Long> {

    // Used by Activity Service simulate-scan endpoint to look up member
    Optional<NfcCard> findByCardUid(String cardUid);

    // Find active card by UID — used to validate entry
    Optional<NfcCard> findByCardUidAndStatus(String cardUid, NfcCardStatus status);

    // All cards belonging to a member
    List<NfcCard> findByMemberProfileId(Long memberProfileId);

    // All cards belonging to a member by userId
    List<NfcCard> findByMemberProfileUserId(Long userId);
}