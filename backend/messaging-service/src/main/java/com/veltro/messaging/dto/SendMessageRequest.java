package com.veltro.messaging.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SendMessageRequest {

    @NotBlank(message = "content is required")
    @Size(max = 5000, message = "content must be 5000 characters or less")
    private String content;
}
