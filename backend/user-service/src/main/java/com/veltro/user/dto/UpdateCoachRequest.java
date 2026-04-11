package com.veltro.user.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdateCoachRequest {

    private String firstname;

    private String lastname;

    private String phone;

    private LocalDate dateOfBirth;

    private String bio;

    private String specialization;

    private String certifications;
}