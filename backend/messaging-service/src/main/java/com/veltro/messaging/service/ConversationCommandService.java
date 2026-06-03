package com.veltro.messaging.service;

import com.veltro.messaging.config.RabbitMQConfig;
import com.veltro.messaging.dto.*;
import com.veltro.messaging.entity.Conversation;
import com.veltro.messaging.entity.Message;
import com.veltro.messaging.entity.SenderRole;
import com.veltro.messaging.event.MessageReceivedEvent;
import com.veltro.messaging.exception.BusinessRuleException;
import com.veltro.messaging.exception.ResourceNotFoundException;
import com.veltro.messaging.repository.ConversationRepository;
import com.veltro.messaging.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConversationCommandService {

    private final ConversationRepository conversationRepo;
    private final MessageRepository messageRepo;
    private final RabbitTemplate rabbitTemplate;

    @Transactional
    public ConversationResponse startConversation(Long memberId, StartConversationRequest req) {
        return conversationRepo.findByMemberIdAndCoachId(memberId, req.getCoachId())
                .map(c -> ConversationResponse.from(c, false))
                .orElseGet(() -> {
                    Conversation conv = new Conversation();
                    conv.setMemberId(memberId);
                    conv.setCoachId(req.getCoachId());
                    return ConversationResponse.from(conversationRepo.save(conv), false);
                });
    }

    @Transactional
    public MessageResponse sendMessage(Long conversationId, Long senderId, String senderRole, SendMessageRequest req) {
        Conversation conv = getConversationForUser(conversationId, senderId, senderRole);

        SenderRole role = senderRole.equals("COACH") ? SenderRole.COACH : SenderRole.MEMBER;

        Message msg = new Message();
        msg.setConversationId(conversationId);
        msg.setSenderId(senderId);
        msg.setSenderRole(role);
        msg.setContent(req.getContent());
        messageRepo.save(msg);

        // Update conversation metadata
        String preview = req.getContent().length() > 100
                ? req.getContent().substring(0, 97) + "…"
                : req.getContent();
        conv.setLastMessageAt(LocalDateTime.now());
        conv.setLastMessagePreview(preview);

        // Increment unread for the recipient
        Long recipientId;
        if (role == SenderRole.MEMBER) {
            conv.setUnreadCountCoach(conv.getUnreadCountCoach() + 1);
            recipientId = conv.getCoachId();
        } else {
            conv.setUnreadCountMember(conv.getUnreadCountMember() + 1);
            recipientId = conv.getMemberId();
        }
        conversationRepo.save(conv);

        // Publish MessageReceived event (best-effort, don't fail the message save on event error)
        try {
            String contentPreview = req.getContent().length() > 50
                    ? req.getContent().substring(0, 47) + "…"
                    : req.getContent();
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.MESSAGING_EXCHANGE,
                    RabbitMQConfig.MESSAGE_RECEIVED_ROUTING_KEY,
                    new MessageReceivedEvent(msg.getId(), conversationId, senderId,
                            "Member/Coach", role, recipientId, contentPreview, msg.getSentAt()));
        } catch (Exception e) {
            log.warn("Failed to publish MessageReceivedEvent for messageId={}: {}", msg.getId(), e.getMessage());
        }

        return MessageResponse.from(msg);
    }

    @Transactional
    public void markConversationRead(Long conversationId, Long userId, String senderRole) {
        Conversation conv = getConversationForUser(conversationId, userId, senderRole);
        SenderRole myRole = senderRole.equals("COACH") ? SenderRole.COACH : SenderRole.MEMBER;

        messageRepo.markAsRead(conversationId, myRole, LocalDateTime.now());

        if (myRole == SenderRole.MEMBER) {
            conv.setUnreadCountMember(0);
        } else {
            conv.setUnreadCountCoach(0);
        }
        conversationRepo.save(conv);
    }

    @Transactional
    public void markAllRead(Long userId, String senderRole) {
        boolean isCoach = "COACH".equals(senderRole);
        List<Conversation> conversations = isCoach
                ? conversationRepo.findByCoachIdOrderByLastMessageAtDesc(userId)
                : conversationRepo.findByMemberIdOrderByLastMessageAtDesc(userId);

        SenderRole myRole = isCoach ? SenderRole.COACH : SenderRole.MEMBER;

        for (Conversation conv : conversations) {
            messageRepo.markAsRead(conv.getId(), myRole, LocalDateTime.now());
            if (isCoach) conv.setUnreadCountCoach(0);
            else conv.setUnreadCountMember(0);
            conversationRepo.save(conv);
        }
    }

    private Conversation getConversationForUser(Long conversationId, Long userId, String role) {
        Conversation conv = conversationRepo.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found: " + conversationId));

        boolean isCoach = "COACH".equals(role);
        boolean isMember = "MEMBER".equals(role);
        boolean belongsToUser = (isCoach && conv.getCoachId().equals(userId))
                || (isMember && conv.getMemberId().equals(userId));

        if (!belongsToUser) {
            throw new BusinessRuleException("You are not a participant in this conversation");
        }
        return conv;
    }
}
