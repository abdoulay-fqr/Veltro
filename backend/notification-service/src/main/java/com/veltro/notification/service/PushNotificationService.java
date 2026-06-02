package com.veltro.notification.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class PushNotificationService {

    // FCM stub — logs to console; real FCM token integration in Phase 6
    public void sendSubscriptionExpiring(Long memberId, String plan, int daysRemaining) {
        log.info("[FCM-STUB] Push notification → memberId={}, message='Your {} subscription expires in {} day(s)'",
                memberId, plan, daysRemaining);
    }
}
