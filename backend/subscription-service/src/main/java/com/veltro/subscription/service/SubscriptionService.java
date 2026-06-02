package com.veltro.subscription.service;

import com.veltro.subscription.config.RabbitMQConfig;
import com.veltro.subscription.dto.CreateSubscriptionRequest;
import com.veltro.subscription.dto.PaymentRecordResponse;
import com.veltro.subscription.dto.SubscriptionResponse;
import com.veltro.subscription.entity.*;
import com.veltro.subscription.event.SubscriptionExpiringEvent;
import com.veltro.subscription.exception.BusinessRuleException;
import com.veltro.subscription.exception.ConflictException;
import com.veltro.subscription.exception.ResourceNotFoundException;
import com.veltro.subscription.repository.PaymentRecordRepository;
import com.veltro.subscription.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionService {

    private static final int MAX_PAUSE_MONTHS_PER_YEAR = 2;

    private final SubscriptionRepository subscriptionRepo;
    private final PaymentRecordRepository paymentRecordRepo;
    private final RabbitTemplate rabbitTemplate;
    private final Clock clock;

    // ── CREATE ───────────────────────────────────────────────────────────────

    @Transactional
    public SubscriptionResponse create(CreateSubscriptionRequest req) {
        boolean hasActive = subscriptionRepo.existsByMemberIdAndStatusIn(
                req.getMemberId(), List.of(SubscriptionStatus.ACTIVE, SubscriptionStatus.PAUSED));
        if (hasActive) {
            throw new ConflictException("Member already has an active or paused subscription. Cancel it first.");
        }

        Subscription sub = buildSubscription(req.getMemberId(), req.getMemberEmail(),
                req.getPlan(), req.isAutoRenew());
        subscriptionRepo.save(sub);

        if (req.getPlan().getPrice().compareTo(BigDecimal.ZERO) > 0) {
            createPaymentRecord(sub.getId(), req.getPlan().getPrice(),
                    req.getPaymentMethod() != null ? req.getPaymentMethod() : PaymentMethod.CARD);
        }

        return SubscriptionResponse.from(sub);
    }

    @Transactional
    public SubscriptionResponse createTrial(Long memberId, String memberEmail) {
        if (subscriptionRepo.existsByMemberIdAndStatusIn(
                memberId, List.of(SubscriptionStatus.ACTIVE, SubscriptionStatus.PAUSED))) {
            log.info("Member {} already has an active subscription, skipping TRIAL creation", memberId);
            return null;
        }
        Subscription sub = buildSubscription(memberId, memberEmail, Plan.TRIAL, false);
        subscriptionRepo.save(sub);
        log.info("TRIAL subscription created for memberId={}", memberId);
        return SubscriptionResponse.from(sub);
    }

    // ── QUERY ────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public SubscriptionResponse getActiveByMemberId(Long memberId) {
        return subscriptionRepo.findFirstByMemberIdAndStatusInOrderByCreatedAtDesc(
                        memberId, List.of(SubscriptionStatus.ACTIVE, SubscriptionStatus.PAUSED))
                .map(SubscriptionResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No active subscription found for memberId: " + memberId));
    }

    @Transactional(readOnly = true)
    public List<SubscriptionResponse> getAllByMemberId(Long memberId) {
        return subscriptionRepo.findByMemberIdOrderByCreatedAtDesc(memberId)
                .stream().map(SubscriptionResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public Page<SubscriptionResponse> findAll(Pageable pageable) {
        return subscriptionRepo.findAll(pageable).map(SubscriptionResponse::from);
    }

    // ── LIFECYCLE ────────────────────────────────────────────────────────────

    @Transactional
    public SubscriptionResponse cancel(Long id) {
        Subscription sub = getOrThrow(id);
        if (sub.getStatus() == SubscriptionStatus.CANCELLED || sub.getStatus() == SubscriptionStatus.EXPIRED) {
            throw new BusinessRuleException("Subscription is already " + sub.getStatus().name().toLowerCase());
        }
        sub.setStatus(SubscriptionStatus.CANCELLED);
        return SubscriptionResponse.from(subscriptionRepo.save(sub));
    }

    @Transactional
    public SubscriptionResponse pause(Long id) {
        Subscription sub = getOrThrow(id);
        if (sub.getStatus() != SubscriptionStatus.ACTIVE) {
            throw new BusinessRuleException("Only ACTIVE subscriptions can be paused");
        }
        if (sub.getPausedMonthsUsed() >= MAX_PAUSE_MONTHS_PER_YEAR) {
            throw new BusinessRuleException(
                    "Maximum pause limit of " + MAX_PAUSE_MONTHS_PER_YEAR + " months per year exceeded");
        }
        sub.setStatus(SubscriptionStatus.PAUSED);
        sub.setPauseStartedAt(LocalDate.now(clock));
        return SubscriptionResponse.from(subscriptionRepo.save(sub));
    }

    @Transactional
    public SubscriptionResponse resume(Long id) {
        Subscription sub = getOrThrow(id);
        if (sub.getStatus() != SubscriptionStatus.PAUSED) {
            throw new BusinessRuleException("Only PAUSED subscriptions can be resumed");
        }
        LocalDate pausedAt = sub.getPauseStartedAt();
        LocalDate today = LocalDate.now(clock);
        long daysOnPause = ChronoUnit.DAYS.between(pausedAt, today);
        long monthsOnPause = (long) Math.ceil(daysOnPause / 30.0);

        // Extend endDate by the paused duration
        sub.setEndDate(sub.getEndDate().plusDays(daysOnPause));
        sub.setPausedMonthsUsed(sub.getPausedMonthsUsed() + (int) monthsOnPause);
        sub.setStatus(SubscriptionStatus.ACTIVE);
        sub.setPauseStartedAt(null);
        return SubscriptionResponse.from(subscriptionRepo.save(sub));
    }

    @Transactional
    public SubscriptionResponse toggleAutoRenew(Long id) {
        Subscription sub = getOrThrow(id);
        sub.setAutoRenew(!sub.isAutoRenew());
        return SubscriptionResponse.from(subscriptionRepo.save(sub));
    }

    // ── INVOICES ─────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<PaymentRecordResponse> getInvoicesByMemberId(Long memberId) {
        List<Subscription> subs = subscriptionRepo.findByMemberIdOrderByCreatedAtDesc(memberId);
        return subs.stream()
                .flatMap(s -> paymentRecordRepo.findBySubscriptionIdOrderByPaidAtDesc(s.getId()).stream())
                .map(PaymentRecordResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public PaymentRecordResponse getInvoiceById(Long memberId, Long invoiceId) {
        PaymentRecord record = paymentRecordRepo.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found: " + invoiceId));
        Subscription sub = getOrThrow(record.getSubscriptionId());
        if (!sub.getMemberId().equals(memberId)) {
            throw new BusinessRuleException("Invoice does not belong to this member");
        }
        return PaymentRecordResponse.from(record);
    }

    // ── AUTO-RENEW (called by scheduler) ─────────────────────────────────────

    @Transactional
    public void renewSubscription(Subscription sub) {
        Plan plan = sub.getPlan();
        sub.setStartDate(sub.getEndDate());
        sub.setEndDate(sub.getEndDate().plusDays(plan.getDurationDays()));
        sub.setStatus(SubscriptionStatus.ACTIVE);
        subscriptionRepo.save(sub);

        if (plan.getPrice().compareTo(BigDecimal.ZERO) > 0) {
            createPaymentRecord(sub.getId(), plan.getPrice(), PaymentMethod.AUTO_RENEWAL);
        }
        log.info("Auto-renewed subscription {} for memberId={}", sub.getId(), sub.getMemberId());
    }

    @Transactional
    public void expireSubscription(Subscription sub) {
        sub.setStatus(SubscriptionStatus.EXPIRED);
        subscriptionRepo.save(sub);
        log.info("Expired subscription {} for memberId={}", sub.getId(), sub.getMemberId());
    }

    public void publishExpiringEvent(Subscription sub) {
        LocalDate today = LocalDate.now(clock);
        int daysRemaining = (int) ChronoUnit.DAYS.between(today, sub.getEndDate());
        SubscriptionExpiringEvent event = new SubscriptionExpiringEvent(
                sub.getId(), sub.getMemberId(), sub.getMemberEmail(),
                sub.getPlan(), sub.getEndDate(), daysRemaining);
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.SUBSCRIPTION_EXCHANGE,
                RabbitMQConfig.SUBSCRIPTION_EXPIRING_ROUTING_KEY,
                event);
        log.info("Published SubscriptionExpiringEvent for subscriptionId={}", sub.getId());
    }

    // ── HELPERS ──────────────────────────────────────────────────────────────

    private Subscription buildSubscription(Long memberId, String memberEmail, Plan plan, boolean autoRenew) {
        LocalDate today = LocalDate.now(clock);
        Subscription sub = new Subscription();
        sub.setMemberId(memberId);
        sub.setMemberEmail(memberEmail);
        sub.setPlan(plan);
        sub.setStatus(SubscriptionStatus.ACTIVE);
        sub.setStartDate(today);
        sub.setEndDate(today.plusDays(plan.getDurationDays()));
        sub.setAutoRenew(autoRenew);
        return sub;
    }

    private PaymentRecord createPaymentRecord(Long subscriptionId, BigDecimal amount, PaymentMethod method) {
        PaymentRecord record = new PaymentRecord();
        record.setSubscriptionId(subscriptionId);
        record.setAmount(amount);
        record.setPaidAt(LocalDateTime.now(clock));
        record.setMethod(method);
        record.setInvoiceRef("INV-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        return paymentRecordRepo.save(record);
    }

    private Subscription getOrThrow(Long id) {
        return subscriptionRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription not found: " + id));
    }
}
