package com.veltro.user.controller;

import com.veltro.common.dto.ApiResponse;
import com.veltro.user.dto.NfcActivateRequest;
import com.veltro.user.dto.NfcCardResponse;
import com.veltro.user.dto.SimulateScanResponse;
import com.veltro.user.service.NfcService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/nfc")
@RequiredArgsConstructor
@Tag(name = "NFC", description = "NFC card management and scanning endpoints")
public class NfcScanController {

    private final NfcService nfcService;

    @Operation(summary = "Activate NFC card for a member")
    @PostMapping("/activate")
    public ResponseEntity<ApiResponse<NfcCardResponse>> activate(
            @RequestBody NfcScanActivateRequest request) {

        NfcCardResponse response = nfcService.activate(
                request.getMemberProfileId(),
                new NfcActivateRequest(request.getCardUid())
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("NFC card activated", response));
    }

    @Operation(summary = "Deactivate NFC card by card UID")
    @PostMapping("/deactivate")
    public ResponseEntity<ApiResponse<NfcCardResponse>> deactivate(
            @RequestBody Map<String, String> body) {

        String cardUid = body.get("cardUid");
        String memberProfileIdStr = body.get("memberProfileId");
        Long memberProfileId = memberProfileIdStr != null ? Long.parseLong(memberProfileIdStr) : null;

        if (cardUid == null || memberProfileId == null) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("cardUid and memberProfileId are required"));
        }

        NfcCardResponse response = nfcService.deactivate(memberProfileId, cardUid);
        return ResponseEntity.ok(ApiResponse.success("NFC card deactivated", response));
    }

    @Operation(summary = "Simulate NFC card scan — returns member info for the given card UID")
    @PostMapping("/simulate-scan")
    public ResponseEntity<ApiResponse<SimulateScanResponse>> simulateScan(
            @RequestBody Map<String, String> body) {

        String cardUid = body.get("cardUid");
        SimulateScanResponse response = nfcService.simulateScan(cardUid);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // Inner DTO for the activate request body
    @lombok.Data
    public static class NfcScanActivateRequest {
        private Long memberProfileId;
        private String cardUid;
    }
}
