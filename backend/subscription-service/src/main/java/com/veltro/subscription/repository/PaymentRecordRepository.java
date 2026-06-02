package com.veltro.subscription.repository;

import com.veltro.subscription.entity.PaymentRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRecordRepository extends JpaRepository<PaymentRecord, Long> {

    List<PaymentRecord> findBySubscriptionIdOrderByPaidAtDesc(Long subscriptionId);

    Optional<PaymentRecord> findByInvoiceRef(String invoiceRef);

    @Query("SELECT SUM(p.amount) FROM PaymentRecord p")
    Optional<BigDecimal> sumAllRevenue();

    @Query(value = "SELECT SUM(amount) FROM payment_record " +
           "WHERE YEAR(paid_at) = YEAR(CURDATE()) AND MONTH(paid_at) = MONTH(CURDATE())",
           nativeQuery = true)
    Optional<BigDecimal> sumCurrentMonthRevenue();

    @Query(value = "SELECT YEAR(paid_at) AS yr, MONTH(paid_at) AS mo, SUM(amount) AS total " +
           "FROM payment_record GROUP BY yr, mo ORDER BY yr DESC, mo DESC",
           nativeQuery = true)
    List<Object[]> findMonthlyRevenue();
}
