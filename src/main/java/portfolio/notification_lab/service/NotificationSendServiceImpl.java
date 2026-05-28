package portfolio.notification_lab.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import portfolio.notification_lab.dto.NotificationRequestDto;
import portfolio.notification_lab.provider.NotificationProvider;
import portfolio.notification_lab.provider.SendResult;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationSendServiceImpl implements NotificationSendService {

    private final NotificationProvider provider;
    private final NotificationRequestStateService stateService;

    // 초안
    private static final int MAX_RETRY_COUNT = 3;
    private static final int NEXT_RETRY_SECONDS = 60;

    @Override
    public void sendOne(NotificationRequestDto request) {
        validateRequest(request);

        // 계산 로직으로 발송 결과 값을 SendResult에 반환
        SendResult result = provider.send(request);

        // 결과 값을 발송 상태로 업데이트(성공)
        if (result.success()) {
            stateService.markSent(request.getId());
            log.debug("알림 발송 성공 - requestId={}", request.getId());

            return;
        }

        // 재시도 가능
        if (result.retryable()) {
            stateService.markRetryableFailure(request.getId(), result.failReason(), NEXT_RETRY_SECONDS, MAX_RETRY_COUNT);
            log.debug("알림 재시도 가능 - requestId={} reason={} nextRetrySeconds={} maxRetryCount={}",
                    request.getId(), result.failReason(), NEXT_RETRY_SECONDS, MAX_RETRY_COUNT);

            return;
        }

        // 재시도 불가능
        stateService.markDeadByNonRetryableFailure(request.getId(), result.failReason());

        log.debug("알림 재시도 불가능 - requestId={} reason={}", request.getId(), result.failReason());
    }

    private void validateRequest(NotificationRequestDto request) {
        if (request == null) {
            throw new IllegalArgumentException("request는 null일 수 없습니다.");
        }

        if (request.getId() == null || request.getId() <= 0) {
            throw new IllegalArgumentException("requestId가 유효하지 않습니다: " + request.getId());
        }
    }
}
