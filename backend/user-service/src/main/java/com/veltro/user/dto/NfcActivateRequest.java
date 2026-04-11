package com.veltro.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class NfcActivateRequest {

    @NotBlank(message = "cardUid is required")
    private String cardUid;
}