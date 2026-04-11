package com.veltro.user.controller;

import com.veltro.common.dto.ApiResponse;
import com.veltro.user.dto.CreateMemberRequest;
import com.veltro.user.dto.MemberResponse;
import com.veltro.user.dto.UpdateMemberRequest;
import com.veltro.user.entity.AccountStatus;
import com.veltro.user.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/users/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    // ── CREATE ──────────────────────────────────────────────────────────────
    // Called by auth-service (or admin) after registering a MEMBER in app_user

    @PostMapping
    public ResponseEntity<ApiResponse<MemberResponse>> create(
            @Valid @RequestBody CreateMemberRequest req) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Member created", memberService.create(req)));
    }

    // ── LIST ─────────────────────────────────────────────────────────────────
    // Admin only — X-User-Role header forwarded by Gateway

    @GetMapping
    public ResponseEntity<ApiResponse<Page<MemberResponse>>> findAll(
            @RequestParam(required = false) AccountStatus status,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable,
            @RequestHeader("X-User-Role") String role) {

        requireAdmin(role);

        Page<MemberResponse> page = status != null
                ? memberService.findByStatus(status, pageable)
                : memberService.findAll(pageable);

        return ResponseEntity.ok(ApiResponse.success(page));
    }

    // ── GET BY ID ────────────────────────────────────────────────────────────

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MemberResponse>> findById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(memberService.findById(id)));
    }

    // ── UPDATE ───────────────────────────────────────────────────────────────

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<MemberResponse>> update(
            @PathVariable Long id,
            @RequestBody UpdateMemberRequest req) {
        return ResponseEntity.ok(ApiResponse.success("Member updated", memberService.update(id, req)));
    }

    // ── AVATAR UPLOAD ────────────────────────────────────────────────────────

    @PostMapping("/{id}/avatar")
    public ResponseEntity<ApiResponse<MemberResponse>> uploadAvatar(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(ApiResponse.success("Avatar uploaded", memberService.uploadAvatar(id, file)));
    }

    // ── SUSPEND / ACTIVATE ───────────────────────────────────────────────────

    @PutMapping("/{id}/suspend")
    public ResponseEntity<ApiResponse<MemberResponse>> suspend(
            @PathVariable Long id,
            @RequestHeader("X-User-Role") String role) {
        requireAdmin(role);
        return ResponseEntity.ok(ApiResponse.success("Member suspended", memberService.suspend(id)));
    }

    @PutMapping("/{id}/activate")
    public ResponseEntity<ApiResponse<MemberResponse>> activate(
            @PathVariable Long id,
            @RequestHeader("X-User-Role") String role) {
        requireAdmin(role);
        return ResponseEntity.ok(ApiResponse.success("Member activated", memberService.activate(id)));
    }

    // ── HELPER ───────────────────────────────────────────────────────────────

    private void requireAdmin(String role) {
        if (!"ADMIN".equals(role) && !"SUPER_ADMIN".equals(role)) {
            throw new com.veltro.user.exception.AccessDeniedException("Admin access required");
        }
    }
}