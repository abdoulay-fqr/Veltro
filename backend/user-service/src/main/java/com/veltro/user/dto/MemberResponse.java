package com.veltro.user.dto;

import com.veltro.user.entity.AccountStatus;
import com.veltro.user.entity.MemberProfile;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class MemberResponse {

    private Long id;
    private Long userId;
    private String email;
    private String firstname;
    private String lastname;
    private String phone;
    private LocalDate dateOfBirth;
    private String avatarUrl;
    private AccountStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Static factory — maps entity to response
    public static MemberResponse from(MemberProfile m) {
        MemberResponse r = new MemberResponse();
        r.setId(m.getId());
        r.setUserId(m.getUserId());
        r.setEmail(m.getEmail());
        r.setFirstname(m.getFirstname());
        r.setLastname(m.getLastname());
        r.setPhone(m.getPhone());
        r.setDateOfBirth(m.getDateOfBirth());
        r.setAvatarUrl(m.getAvatarUrl());
        r.setStatus(m.getStatus());
        r.setCreatedAt(m.getCreatedAt());
        r.setUpdatedAt(m.getUpdatedAt());
        return r;
    }
}