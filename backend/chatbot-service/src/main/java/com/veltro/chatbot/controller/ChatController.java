package com.veltro.chatbot.controller;

import com.veltro.chatbot.dto.ChatRequest;
import com.veltro.chatbot.dto.ChatResponse;
import com.veltro.chatbot.service.ChatbotService;
import com.veltro.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
@Tag(name = "Chatbot", description = "Veltro AI Assistant powered by Gemini 1.5 Flash")
public class ChatController {

    private final ChatbotService chatbotService;

    @Operation(summary = "Send a message to Veltro Assistant",
               description = "Pass the full conversation history for multi-turn context. " +
                             "Returns the AI reply and the updated history to store on the client.")
    @PostMapping("/message")
    public ResponseEntity<ApiResponse<ChatResponse>> message(
            @Valid @RequestBody ChatRequest request) {
        return ResponseEntity.ok(ApiResponse.success(chatbotService.chat(request)));
    }
}
