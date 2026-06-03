package com.veltro.booking.dto;

import lombok.Data;

@Data
public class AttendanceRecordDto {
    private Long memberId;
    private String memberEmail;
    private boolean present;
}
