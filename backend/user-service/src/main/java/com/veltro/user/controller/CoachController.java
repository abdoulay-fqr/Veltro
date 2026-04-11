package com.veltro.user.controller;

import com.veltro.common.dto.ApiResponse;
import com.veltro.user.dto.CoachResponse;
import com.veltro.user.dto.CreateCoachRequest;
import com.veltro.user.dto.UpdateCoachRequest;
import com.veltro.user.entity.AccountStatus;
import com.veltro.user.service.CoachService;
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
@RequestMapping("/api/v1/users/coaches")
@RequiredArgsConstructor
public class CoachController {

    private final CoachService coachService;

    // ── CREATE ──────────────────────────────────────────────────────────────

    @PostMapping
    public ResponseEntity<ApiResponse<CoachResponse>> create(
            @Valid @RequestBody CreateCoachRequest req) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Coach created", coachService.create(req)));
    }

    // ── LIST ─────────────────────────────────────────────────────────────────

    @GetMapping
    public ResponseEntity<ApiResponse<Page<CoachResponse>>> findAll(
            @RequestParam(required = false) AccountStatus status,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable,
            @RequestHeader("X-User-Role") String role) {

        requireAdmin(role);

        Page<CoachResponse> page = status != null
                ? coachService.findByStatus(status, pageable)
                : coachService.findAll(pageable);

        return ResponseEntity.ok(ApiResponse.success(page));
    }

    // ── GET BY ID ────────────────────────────────────────────────────────────

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CoachResponse>> findById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(coachService.findById(id)));
    }

    // ── UPDATE ───────────────────────────────────────────────────────────────

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CoachResponse>> update(
            @PathVariable Long id,
            @RequestBody UpdateCoachRequest req) {
        return ResponseEntity.ok(ApiResponse.success("Coach updated", coachService.update(id, req)));
    }

    // ── AVATAR UPLOAD ────────────────────────────────────────────────────────

    @PostMapping("/{id}/avatar")
    public ResponseEntity<ApiResponse<CoachResponse>> uploadAvatar(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(ApiResponse.success("Avatar uploaded", coachService.uploadAvatar(id, file)));
    }

    // ── SUSPEND / ACTIVATE ───────────────────────────────────────────────────

    @PutMapping("/{id}/suspend")
    public ResponseEntity<ApiResponse<CoachResponse>> suspend(
            @PathVariable Long id,
            @RequestHeader("X-User-Role") String role) {
        requireAdmin(role);
        return ResponseEntity.ok(ApiResponse.success("Coach suspended", coachService.suspend(id)));
    }

    @PutMapping("/{id}/activate")
    public ResponseEntity<ApiResponse<CoachResponse>> activate(
            @PathVariable Long id,
            @RequestHeader("X-User-Role") String role) {
        requireAdmin(role);
        return ResponseEntity.ok(ApiResponse.success("Coach activated", coachService.activate(id)));
    }

    // ── HELPER ───────────────────────────────────────────────────────────────

    private void requireAdmin(String role) {
        if (!"ADMIN".equals(role) && !"SUPER_ADMIN".equals(role)) {
            throw new com.veltro.user.exception.AccessDeniedException("Admin access required");
        }
    }
}