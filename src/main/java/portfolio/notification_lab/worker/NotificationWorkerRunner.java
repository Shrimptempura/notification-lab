package portfolio.notification_lab.worker;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import portfolio.notification_lab.dto.NotificationRequestDto;
import portfolio.notification_lab.service.NotificationSendService;
import portfolio.notification_lab.service.NotificationWorkerService;

import java.util.List;

// 처리 가능한 요청을 RESERVED로 선점하고, 선점된 요청을 하나씩 발송 처리 서비스로 넘긴다.
// NotificationWorkerService: PENDING/재시도 가능한 FAILED를 RESERVED로 선점
// NotificationSendService: RESERVED 요청 1건을 발송 처리하고 SENT / FAILED / DEAD 처리
// NotificationWorkerRunner: 위 두개의 서비스 연결하여 worker 1회 실행 흐름
// 수십만건을 한번이 아닌 limit 단위로 잘라서 처리
// 추후 실제 반복, 스케줄링 등은 나중에 추가 예정
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationWorkerRunner {

    private final NotificationWorkerService workerService;
    private final NotificationSendService sendService;

    public int runOnce(int limit, int maxRetryCount) {
        validateLimit(limit);
        validateMaxRetryCount(maxRetryCount);

        List<NotificationRequestDto> reservedRequests = workerService.reserveRunnableRequests(limit, maxRetryCount);

        if (reservedRequests.isEmpty()) {
            log.debug("worker 실행 완료 - 처리할 알림 요청 없음. limit={} maxRetryCount={}", limit, maxRetryCount);

            return 0;
        }

        for (NotificationRequestDto request : reservedRequests) {
            sendService.sendOne(request);
        }

        log.info("worker 실행 완료 - reservedCount={}, limit={}, maxRetryCount={}", reservedRequests.size(), limit, maxRetryCount);

        return reservedRequests.size();
    }

    private void validateLimit(int limit) {
        if (limit <= 0) {
            throw new IllegalArgumentException("limit는 1 이상이어야 합니다. " + limit);
        }
    }

    private void validateMaxRetryCount(int maxRetryCount) {
        if (maxRetryCount < 0) {
            throw new IllegalArgumentException("maxRetryCount는 0 이상이어야 합니다. " + maxRetryCount);
        }
    }
}
