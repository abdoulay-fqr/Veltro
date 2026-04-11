package com.veltro.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateCoachRequest {

    @NotNull(message = "userId is required")
    private Long userId;          // app_user.id from auth-service

    @NotBlank(message = "identifier is required")
    private String identifier;    // email — used for UserCreatedEvent

    @NotBlank(message = "firstname is required")
    private String firstname;

    @NotBlank(message = "lastname is required")
    private String lastname;

    private String phone;

    private LocalDate dateOfBirth;

    private String bio;

    private String specialization;

    private String certifications;
}