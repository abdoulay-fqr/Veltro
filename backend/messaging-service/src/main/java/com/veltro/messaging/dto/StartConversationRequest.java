package com.veltro.messaging.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StartConversationRequest {

    @NotNull(message = "coachId is required")
    private Long coachId;
}
