package portfolio.notification_lab.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import portfolio.notification_lab.config.NotificationRetryProperties;
import portfolio.notification_lab.config.NotificationWorkerProperties;
import portfolio.notification_lab.worker.NotificationWorkerRunner;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationWorkerScheduler {

    private final NotificationWorkerRunner workerRunner;
    private final NotificationWorkerProperties workerProperties;
    private final NotificationRetryProperties retryProperties;

    @Scheduled(fixedDelayString = "${notification.worker.fixed-delay-ms}")
    public void runWorker() {
        if (!workerProperties.enabled()) {
            return;
        }

        // runOnce는 조건(PENDING, 재시도 가능한 FAILED)을 만족하면 RESERVED로 limit 개수만큼 반환하여 1회 발송(안에서 반복문)
        int processCount = workerRunner.runOnce(workerProperties.limit(), retryProperties.maxRetryCount());

        // 전체 테이블 처리가 아님
        if (processCount > 0) {
            log.info("알림 worker 스케줄 실행 완료 - processCount={} limit={} maxRetryCount={}", processCount, workerProperties.limit(), retryProperties.maxRetryCount());
        }
    }
}
