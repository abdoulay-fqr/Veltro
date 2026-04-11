package com.veltro.user.controller;

import com.veltro.common.dto.ApiResponse;
import com.veltro.user.dto.NfcActivateRequest;
import com.veltro.user.dto.NfcCardResponse;
import com.veltro.user.dto.SimulateScanResponse;
import com.veltro.user.service.NfcService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users/members/{memberProfileId}/nfc")
@RequiredArgsConstructor
public class NfcController {

    private final NfcService nfcService;

    // POST /api/v1/users/members/{memberProfileId}/nfc/activate
    @PostMapping("/activate")
    public ResponseEntity<ApiResponse<NfcCardResponse>> activate(
            @PathVariable Long memberProfileId,
            @Valid @RequestBody NfcActivateRequest request) {

        NfcCardResponse response = nfcService.activate(memberProfileId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    // POST /api/v1/users/members/{memberProfileId}/nfc/deactivate/{cardUid}
    @PostMapping("/deactivate/{cardUid}")
    public ResponseEntity<ApiResponse<NfcCardResponse>> deactivate(
            @PathVariable Long memberProfileId,
            @PathVariable String cardUid) {

        NfcCardResponse response = nfcService.deactivate(memberProfileId, cardUid);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // GET /api/v1/users/members/{memberProfileId}/nfc
    @GetMapping
    public ResponseEntity<ApiResponse<List<NfcCardResponse>>> getCards(
            @PathVariable Long memberProfileId) {

        return ResponseEntity.ok(ApiResponse.success(nfcService.getCardsForMember(memberProfileId)));
    }
}