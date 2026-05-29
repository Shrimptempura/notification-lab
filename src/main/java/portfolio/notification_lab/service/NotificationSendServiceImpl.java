package portfolio.notification_lab.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import portfolio.notification_lab.config.NotificationRetryProperties;
import portfolio.notification_lab.dto.NotificationRequestDto;
import portfolio.notification_lab.provider.NotificationProvider;
import portfolio.notification_lab.provider.SendResult;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationSendServiceImpl implements NotificationSendService {

    private final NotificationProvider provider;
    private final NotificationRequestStateService stateService;
    private final NotificationRetryProperties retryProperties;

    @Override
    public void sendOne(NotificationRequestDto request) {
        validateRequest(request);

        Long requestId = request.getId();

        // 계산 로직으로 발송 결과 값을 SendResult에 반환
        SendResult result = provider.send(request);

        // 결과 값을 발송 상태로 업데이트(성공)
        if (result.success()) {
            stateService.markSent(requestId);
            log.debug("알림 발송 성공 - requestId={}", requestId);

            return;
        }

        String failReason = result.failReason();

        // 재시도 가능
        if (result.retryable()) {
            int nextRetrySeconds = retryProperties.nextRetrySeconds();
            int maxRetryCount = retryProperties.maxRetryCount();

            stateService.markRetryableFailure(requestId, failReason, nextRetrySeconds, maxRetryCount);
            log.debug("알림 재시도 가능 - requestId={} reason={} nextRetrySeconds={} maxRetryCount={}",
                    requestId, failReason, nextRetrySeconds, maxRetryCount);

            return;
        }

        // 재시도 불가능
        stateService.markDeadByNonRetryableFailure(requestId, failReason);

        log.debug("알림 재시도 불가능 - requestId={} reason={}", requestId, failReason);
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
