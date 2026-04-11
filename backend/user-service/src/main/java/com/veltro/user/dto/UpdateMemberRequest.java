package com.veltro.user.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdateMemberRequest {

    private String firstname;

    private String lastname;

    private String phone;

    private LocalDate dateOfBirth;
}