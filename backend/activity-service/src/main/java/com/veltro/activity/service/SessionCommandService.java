package com.veltro.activity.service;

import com.veltro.activity.config.RabbitMQConfig;
import com.veltro.activity.dto.SessionRequest;
import com.veltro.activity.dto.SessionResponse;
import com.veltro.activity.entity.MachineSession;
import com.veltro.activity.event.MachineSessionRecordedEvent;
import com.veltro.activity.repository.MachineSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class SessionCommandService {

    private final MachineSessionRepository sessionRepo;
    private final RabbitTemplate rabbitTemplate;
    private final Clock clock;

    @Transactional
    public SessionResponse recordSession(SessionRequest req) {
        MachineSession session = new MachineSession();
        session.setMemberId(req.getMemberId());
        session.setMemberEmail(req.getMemberEmail());
        session.setMachineType(req.getMachineType());
        session.setDurationMinutes(req.getDurationMinutes());
        session.setCaloriesBurned(req.getCaloriesBurned());
        session.setDistanceKm(req.getDistanceKm());
        session.setAvgHeartRate(req.getAvgHeartRate());
        session.setRecordedAt(LocalDateTime.now(clock));
        sessionRepo.save(session);

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.ACTIVITY_EXCHANGE,
                RabbitMQConfig.MACHINE_SESSION_RECORDED_ROUTING_KEY,
                new MachineSessionRecordedEvent(session.getId(), session.getMemberId(),
                        session.getMachineType(), session.getDurationMinutes(),
                        session.getCaloriesBurned(), session.getRecordedAt()));

        log.info("MachineSession recorded: memberId={}, type={}, duration={}min",
                req.getMemberId(), req.getMachineType(), req.getDurationMinutes());
        return SessionResponse.from(session);
    }
}
