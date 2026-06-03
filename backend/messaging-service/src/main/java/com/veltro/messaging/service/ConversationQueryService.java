package com.veltro.messaging.service;

import com.veltro.messaging.dto.ConversationResponse;
import com.veltro.messaging.dto.MessageResponse;
import com.veltro.messaging.exception.BusinessRuleException;
import com.veltro.messaging.exception.ResourceNotFoundException;
import com.veltro.messaging.repository.ConversationRepository;
import com.veltro.messaging.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ConversationQueryService {

    private final ConversationRepository conversationRepo;
    private final MessageRepository messageRepo;

    @Transactional(readOnly = true)
    public List<ConversationResponse> listConversations(Long userId, String role) {
        boolean isCoach = "COACH".equals(role);
        var conversations = isCoach
                ? conversationRepo.findByCoachIdOrderByLastMessageAtDesc(userId)
                : conversationRepo.findByMemberIdOrderByLastMessageAtDesc(userId);
        return conversations.stream().map(c -> ConversationResponse.from(c, isCoach)).toList();
    }

    @Transactional(readOnly = true)
    public Page<MessageResponse> getMessages(Long conversationId, Long userId, String role, Pageable pageable) {
        var conv = conversationRepo.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found: " + conversationId));

        boolean isCoach = "COACH".equals(role);
        if ((isCoach && !conv.getCoachId().equals(userId)) || (!isCoach && !conv.getMemberId().equals(userId))) {
            throw new BusinessRuleException("You are not a participant in this conversation");
        }

        return messageRepo.findByConversationIdOrderBySentAtAsc(conversationId, pageable)
                .map(MessageResponse::from);
    }

    @Transactional(readOnly = true)
    public int getUnreadCount(Long userId, String role) {
        boolean isCoach = "COACH".equals(role);
        return isCoach
                ? conversationRepo.sumUnreadForCoach(userId)
                : conversationRepo.sumUnreadForMember(userId);
    }
}
