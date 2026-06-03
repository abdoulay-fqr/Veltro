package com.veltro.messaging.repository;

import com.veltro.messaging.entity.Message;
import com.veltro.messaging.entity.SenderRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    Page<Message> findByConversationIdOrderBySentAtAsc(Long conversationId, Pageable pageable);

    // Mark all messages from the other party as read
    @Modifying
    @Query("UPDATE Message m SET m.readAt = :now WHERE m.conversationId = :conversationId " +
           "AND m.senderRole <> :myRole AND m.readAt IS NULL")
    int markAsRead(@Param("conversationId") Long conversationId,
                   @Param("myRole") SenderRole myRole,
                   @Param("now") LocalDateTime now);
}
