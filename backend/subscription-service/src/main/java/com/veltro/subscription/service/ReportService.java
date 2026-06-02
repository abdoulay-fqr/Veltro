package com.veltro.subscription.service;

import com.veltro.subscription.entity.SubscriptionStatus;
import com.veltro.subscription.repository.PaymentRecordRepository;
import com.veltro.subscription.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final SubscriptionRepository subscriptionRepo;
    private final PaymentRecordRepository paymentRecordRepo;

    @Transactional(readOnly = true)
    public Map<String, Object> getSummary() {
        long totalActive  = subscriptionRepo.countByStatus(SubscriptionStatus.ACTIVE);
        long totalPaused  = subscriptionRepo.countByStatus(SubscriptionStatus.PAUSED);
        long totalTrials  = subscriptionRepo.countByPlanAndStatus(com.veltro.subscription.entity.Plan.TRIAL, SubscriptionStatus.ACTIVE);
        BigDecimal totalRevenue  = paymentRecordRepo.sumAllRevenue().orElse(BigDecimal.ZERO);
        BigDecimal monthRevenue  = paymentRecordRepo.sumCurrentMonthRevenue().orElse(BigDecimal.ZERO);

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("totalActive", totalActive);
        summary.put("totalPaused", totalPaused);
        summary.put("trialsActive", totalTrials);
        summary.put("totalRevenue", totalRevenue);
        summary.put("currentMonthRevenue", monthRevenue);
        return summary;
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getMonthlyRevenue() {
        List<Object[]> rows = paymentRecordRepo.findMonthlyRevenue();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object[] row : rows) {
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("year",  row[0]);
            entry.put("month", row[1]);
            entry.put("total", row[2]);
            result.add(entry);
        }
        return result;
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getPlanDistribution() {
        List<Object[]> rows = subscriptionRepo.countByPlanForActive();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object[] row : rows) {
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("plan",  row[0]);
            entry.put("count", row[1]);
            result.add(entry);
        }
        return result;
    }
}
