package com.veltro.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class NfcActivateRequest {

    @NotBlank(message = "cardUid is required")
    private String cardUid;
}