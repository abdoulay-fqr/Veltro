package com.veltro.messaging.controller;

import com.veltro.common.dto.ApiResponse;
import com.veltro.messaging.dto.*;
import com.veltro.messaging.service.ConversationCommandService;
import com.veltro.messaging.service.ConversationQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/conversations")
@RequiredArgsConstructor
@Tag(name = "Conversations", description = "Coach-member messaging")
public class ConversationController {

    private final ConversationCommandService commandService;
    private final ConversationQueryService queryService;

    @Operation(summary = "List conversations for current user")
    @GetMapping
    public ResponseEntity<ApiResponse<List<ConversationResponse>>> list(
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Role") String role) {
        return ResponseEntity.ok(ApiResponse.success(queryService.listConversations(userId, role)));
    }

    @Operation(summary = "Total unread message count for current user (badge)")
    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<Map<String, Integer>>> unreadCount(
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Role") String role) {
        int count = queryService.getUnreadCount(userId, role);
        return ResponseEntity.ok(ApiResponse.success(Map.of("unreadCount", count)));
    }

    @Operation(summary = "Mark all conversations as read for current user")
    @PutMapping("/read-all")
    public ResponseEntity<ApiResponse<Void>> readAll(
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Role") String role) {
        commandService.markAllRead(userId, role);
        return ResponseEntity.ok(ApiResponse.success("All conversations marked as read"));
    }

    @Operation(summary = "Start new conversation (MEMBER initiates with coachId)")
    @PostMapping
    public ResponseEntity<ApiResponse<ConversationResponse>> start(
            @Valid @RequestBody StartConversationRequest req,
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Role") String role) {

        if (!"MEMBER".equals(role)) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                    .body(ApiResponse.error("Only members can initiate conversations"));
        }
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Conversation ready", commandService.startConversation(userId, req)));
    }

    @Operation(summary = "Get paginated messages (oldest first)")
    @GetMapping("/{id}/messages")
    public ResponseEntity<ApiResponse<Page<MessageResponse>>> getMessages(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Role") String role,
            @PageableDefault(size = 30) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(queryService.getMessages(id, userId, role, pageable)));
    }

    @Operation(summary = "Send a message to a conversation")
    @PostMapping("/{id}/messages")
    public ResponseEntity<ApiResponse<MessageResponse>> sendMessage(
            @PathVariable Long id,
            @Valid @RequestBody SendMessageRequest req,
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Role") String role) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Message sent", commandService.sendMessage(id, userId, role, req)));
    }

    @Operation(summary = "Mark all messages in conversation as read for current user")
    @PutMapping("/{id}/read")
    public ResponseEntity<ApiResponse<Void>> markRead(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Role") String role) {
        commandService.markConversationRead(id, userId, role);
        return ResponseEntity.ok(ApiResponse.success("Marked as read"));
    }
}
