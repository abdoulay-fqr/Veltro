package com.veltro.activity.dto;

import com.veltro.activity.entity.Direction;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EntryRequest {

    @NotBlank(message = "cardUid is required")
    private String cardUid;

    @NotNull(message = "direction is required")
    private Direction direction;
}
