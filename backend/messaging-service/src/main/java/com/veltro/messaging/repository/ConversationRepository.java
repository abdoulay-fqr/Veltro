package com.veltro.messaging.repository;

import com.veltro.messaging.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    List<Conversation> findByMemberIdOrderByLastMessageAtDesc(Long memberId);

    List<Conversation> findByCoachIdOrderByLastMessageAtDesc(Long coachId);

    Optional<Conversation> findByMemberIdAndCoachId(Long memberId, Long coachId);

    @Query("SELECT COALESCE(SUM(c.unreadCountMember), 0) FROM Conversation c WHERE c.memberId = :memberId")
    int sumUnreadForMember(@Param("memberId") Long memberId);

    @Query("SELECT COALESCE(SUM(c.unreadCountCoach), 0) FROM Conversation c WHERE c.coachId = :coachId")
    int sumUnreadForCoach(@Param("coachId") Long coachId);
}
