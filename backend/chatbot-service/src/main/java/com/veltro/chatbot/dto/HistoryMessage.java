package com.veltro.chatbot.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HistoryMessage {

    @Pattern(regexp = "user|model", message = "role must be 'user' or 'model'")
    private String role;

    @NotBlank(message = "content cannot be blank")
    private String content;
}
