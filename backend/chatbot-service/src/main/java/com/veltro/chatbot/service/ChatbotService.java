package com.veltro.chatbot.service;

import com.veltro.chatbot.dto.ChatRequest;
import com.veltro.chatbot.dto.ChatResponse;
import com.veltro.chatbot.dto.HistoryMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatbotService {

    private final GeminiService geminiService;
    private final ContextEnrichmentService contextService;
    private final RateLimiterService rateLimiter;

    public ChatResponse chat(ChatRequest request) {
        // 1. Rate limiting
        rateLimiter.checkAndRecord(request.getUserId());

        // 2. Member context (cached 5 min per Caffeine config)
        String memberContext;
        try {
            memberContext = contextService.buildMemberContext(request.getUserId());
        } catch (Exception e) {
            log.warn("[Chatbot] Context enrichment failed for userId={}, proceeding without context", request.getUserId());
            memberContext = "Current member context: unavailable";
        }

        // 3. Call Gemini with history + current message
        String reply = geminiService.generateContent(memberContext, request.getHistory(), request.getMessage());

        // 4. Build updated history for client to store
        List<HistoryMessage> updatedHistory = new ArrayList<>(request.getHistory());
        updatedHistory.add(new HistoryMessage("user", request.getMessage()));
        updatedHistory.add(new HistoryMessage("model", reply));

        log.info("[Chatbot] Message processed for userId={}, historySize={}", request.getUserId(), updatedHistory.size());
        return new ChatResponse(reply, updatedHistory);
    }
}
