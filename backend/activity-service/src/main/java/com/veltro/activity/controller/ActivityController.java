package com.veltro.activity.controller;

import com.veltro.activity.dto.*;
import com.veltro.activity.exception.BusinessRuleException;
import com.veltro.activity.service.ActivityQueryService;
import com.veltro.activity.service.EntryCommandService;
import com.veltro.activity.service.SessionCommandService;
import com.veltro.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/activity")
@RequiredArgsConstructor
@Tag(name = "Activity", description = "Gym entry/exit, machine sessions, and performance analytics")
public class ActivityController {

    private final EntryCommandService entryCmd;
    private final SessionCommandService sessionCmd;
    private final ActivityQueryService queryService;

    // ── Commands ─────────────────────────────────────────────────────────────

    @Operation(summary = "Record gym entry or exit via NFC card scan")
    @PostMapping("/entry")
    public ResponseEntity<ApiResponse<EntryResponse>> recordEntry(
            @Valid @RequestBody EntryRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Entry recorded", entryCmd.recordEntry(req)));
    }

    @Operation(summary = "Record a machine session for a member")
    @PostMapping("/session")
    public ResponseEntity<ApiResponse<SessionResponse>> recordSession(
            @Valid @RequestBody SessionRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Session recorded", sessionCmd.recordSession(req)));
    }

    // ── Member queries ────────────────────────────────────────────────────────

    @Operation(summary = "Get paginated entry history for a member")
    @GetMapping("/entries/{memberId}")
    public ResponseEntity<ApiResponse<Page<EntryResponse>>> getEntries(
            @PathVariable Long memberId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(queryService.getEntriesByMember(memberId, from, to, pageable)));
    }

    @Operation(summary = "Get paginated machine session history for a member")
    @GetMapping("/sessions/{memberId}")
    public ResponseEntity<ApiResponse<Page<SessionResponse>>> getSessions(
            @PathVariable Long memberId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(queryService.getSessionsByMember(memberId, pageable)));
    }

    @Operation(summary = "Get performance stats for a member (streak, PR, calories, etc.)")
    @GetMapping("/stats/{memberId}")
    public ResponseEntity<ApiResponse<MemberStatsResponse>> getMemberStats(
            @PathVariable Long memberId) {
        return ResponseEntity.ok(ApiResponse.success(queryService.getMemberStats(memberId)));
    }

    // ── Admin queries ─────────────────────────────────────────────────────────

    @Operation(summary = "Get last 20 entries across all members (ADMIN only, live log)")
    @GetMapping("/entries/live")
    public ResponseEntity<ApiResponse<List<EntryResponse>>> getLiveEntries(
            @RequestHeader(value = "X-User-Role", defaultValue = "") String role) {
        if (!"ADMIN".equals(role) && !"SUPER_ADMIN".equals(role)) {
            throw new BusinessRuleException("ADMIN access required for live entry log");
        }
        return ResponseEntity.ok(ApiResponse.success(queryService.getLiveEntries()));
    }

    @Operation(summary = "Get admin occupancy stats: heatmap, peak hour, daily/weekly entries")
    @GetMapping("/stats/admin")
    public ResponseEntity<ApiResponse<AdminStatsResponse>> getAdminStats(
            @RequestHeader(value = "X-User-Role", defaultValue = "") String role) {
        if (!"ADMIN".equals(role) && !"SUPER_ADMIN".equals(role)) {
            throw new BusinessRuleException("ADMIN access required");
        }
        return ResponseEntity.ok(ApiResponse.success(queryService.getAdminStats()));
    }
}
