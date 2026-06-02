package com.veltro.subscription.dto;

import com.veltro.subscription.entity.PaymentMethod;
import com.veltro.subscription.entity.PaymentRecord;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PaymentRecordResponse {

    private Long id;
    private Long subscriptionId;
    private BigDecimal amount;
    private LocalDateTime paidAt;
    private PaymentMethod method;
    private String invoiceRef;
    private LocalDateTime createdAt;

    public static PaymentRecordResponse from(PaymentRecord p) {
        PaymentRecordResponse r = new PaymentRecordResponse();
        r.setId(p.getId());
        r.setSubscriptionId(p.getSubscriptionId());
        r.setAmount(p.getAmount());
        r.setPaidAt(p.getPaidAt());
        r.setMethod(p.getMethod());
        r.setInvoiceRef(p.getInvoiceRef());
        r.setCreatedAt(p.getCreatedAt());
        return r;
    }
}
