package com.veltro.user.controller;

import com.veltro.common.dto.ApiResponse;
import com.veltro.user.dto.HealthProfileRequest;
import com.veltro.user.dto.HealthProfileResponse;
import com.veltro.user.service.HealthProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users/members/{memberProfileId}/health")
@RequiredArgsConstructor
@Tag(name = "HealthProfile", description = "Member health profile management")
public class HealthProfileController {

    private final HealthProfileService healthProfileService;

    @Operation(summary = "Get health profile for a member")
    @GetMapping
    public ResponseEntity<ApiResponse<HealthProfileResponse>> get(@PathVariable Long memberProfileId) {
        return ResponseEntity.ok(ApiResponse.success(healthProfileService.findByMemberProfileId(memberProfileId)));
    }

    @Operation(summary = "Create or update health profile for a member")
    @PutMapping
    public ResponseEntity<ApiResponse<HealthProfileResponse>> upsert(
            @PathVariable Long memberProfileId,
            @RequestBody HealthProfileRequest req) {
        return ResponseEntity.ok(ApiResponse.success("Health profile updated", healthProfileService.upsert(memberProfileId, req)));
    }
}
