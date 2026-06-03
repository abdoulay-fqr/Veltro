package com.veltro.activity.client;

import com.veltro.activity.exception.ServiceUnavailableException;
import com.veltro.common.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(
        name = "nfc-client",
        url = "${clients.user.url}",
        fallbackFactory = NfcClient.NfcClientFallback.class
)
public interface NfcClient {

    @PostMapping("/api/v1/nfc/simulate-scan")
    ApiResponse<NfcScanResult> simulateScan(@RequestBody Map<String, String> body);

    record NfcScanResult(
            String cardUid,
            String cardStatus,       // "ACTIVE" or "INACTIVE"
            Long memberId,
            Long userId,
            String firstname,
            String lastname,
            String phone,
            String avatarUrl,
            String accountStatus
    ) {}

    @Component
    @Slf4j
    class NfcClientFallback implements FallbackFactory<NfcClient> {
        @Override
        public NfcClient create(Throwable cause) {
            return body -> {
                log.error("User-service unavailable for NFC scan of cardUid={}: {}", body.get("cardUid"), cause.getMessage());
                throw new ServiceUnavailableException(
                        "Card validation unavailable — user service is down. Please try again.");
            };
        }
    }
}
