package portfolio.notification_lab.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import portfolio.notification_lab.config.NotificationRecoveryProperties;
import portfolio.notification_lab.service.NotificationRequestStateService;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationRecoveryScheduler {

    private final NotificationRequestStateService stateService;
    private final NotificationRecoveryProperties recoveryProperties;

    @Scheduled(fixedDelayString = "${notification.recovery.fixed-delay-ms}")
    public void recoverExpiredReservations() {
        if (!recoveryProperties.enabled()) {
            return;
        }

        int timeoutMinutes = recoveryProperties.timeoutMinutes();

        int releasedCount = stateService.releaseExpiredReservations(timeoutMinutes);

        if (releasedCount > 0) {
            log.info("알림 recovery 스케줄 실행 완료- releasedCount={} timeoutMinutes={}", releasedCount, timeoutMinutes);
        }
    }
}
