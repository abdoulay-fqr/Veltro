package com.veltro.user.dto;

import com.veltro.user.entity.AccountStatus;
import com.veltro.user.entity.CoachProfile;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class CoachResponse {

    private Long id;
    private Long userId;
    private String firstname;
    private String lastname;
    private String phone;
    private LocalDate dateOfBirth;
    private String avatarUrl;
    private String bio;
    private String specialization;
    private String certifications;
    private AccountStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Static factory — maps entity to response
    public static CoachResponse from(CoachProfile c) {
        CoachResponse r = new CoachResponse();
        r.setId(c.getId());
        r.setUserId(c.getUserId());
        r.setFirstname(c.getFirstname());
        r.setLastname(c.getLastname());
        r.setPhone(c.getPhone());
        r.setDateOfBirth(c.getDateOfBirth());
        r.setAvatarUrl(c.getAvatarUrl());
        r.setBio(c.getBio());
        r.setSpecialization(c.getSpecialization());
        r.setCertifications(c.getCertifications());
        r.setStatus(c.getStatus());
        r.setCreatedAt(c.getCreatedAt());
        r.setUpdatedAt(c.getUpdatedAt());
        return r;
    }
}