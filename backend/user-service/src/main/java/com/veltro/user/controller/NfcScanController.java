package com.veltro.user.controller;

import com.veltro.common.dto.ApiResponse;
import com.veltro.user.dto.SimulateScanResponse;
import com.veltro.user.service.NfcService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/nfc")
@RequiredArgsConstructor
public class NfcScanController {

    private final NfcService nfcService;

    // POST /api/v1/nfc/simulate-scan
    // Body: { "cardUid": "card001" }
    // Called by Node-RED flows and Activity Service in Phase 5
    @PostMapping("/simulate-scan")
    public ResponseEntity<ApiResponse<SimulateScanResponse>> simulateScan(
            @RequestBody java.util.Map<String, String> body) {

        String cardUid = body.get("cardUid");
        SimulateScanResponse response = nfcService.simulateScan(cardUid);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}