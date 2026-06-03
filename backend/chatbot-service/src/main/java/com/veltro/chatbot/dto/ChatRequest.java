package com.veltro.chatbot.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ChatRequest {

    @NotNull(message = "userId is required")
    private Long userId;

    @NotBlank(message = "message cannot be blank")
    @Size(max = 2000, message = "message must be 2000 characters or less")
    private String message;

    private List<HistoryMessage> history = new ArrayList<>();
}
