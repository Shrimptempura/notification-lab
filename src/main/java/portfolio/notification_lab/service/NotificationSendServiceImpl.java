package portfolio.notification_lab.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import portfolio.notification_lab.config.NotificationRetryProperties;
import portfolio.notification_lab.domain.notification.NotificationStatus;
import portfolio.notification_lab.dto.NotificationRequestDto;
import portfolio.notification_lab.provider.NotificationProvider;
import portfolio.notification_lab.provider.SendResult;
import portfolio.notification_lab.recorder.SendAttemptRecorder;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationSendServiceImpl implements NotificationSendService {

    private final NotificationProvider provider;
    private final NotificationRequestStateService stateService;
    private final NotificationRetryProperties retryProperties;
    private final SendAttemptRecorder sendAttemptRecorder;

    // 중요* 외부 api 사용하면 트랜잭션과 꼭 분리해야함 중복발송 조심
    @Transactional
    @Override
    public void sendOne(NotificationRequestDto request) {
        validateRequest(request);

        Long requestId = request.getId();

        // 계산 로직으로 발송 결과 값을 SendResult에 반환
        SendResult result = provider.send(request);

        // 결과 값을 발송 상태로 업데이트(성공)
        if (result.success()) {
            stateService.markSent(requestId);
            sendAttemptRecorder.record(request, result, NotificationStatus.SENT);
            log.debug("알림 발송 성공 - requestId={}", requestId);

            return;
        }

        String failReason = result.failReason();

        // 재시도 가능
        if (result.retryable()) {
            NotificationStatus finalStatus = handleRetryableFailure(request, requestId, failReason);
            sendAttemptRecorder.record(request, result, finalStatus);

            return;
        }

        // 재시도 불가능
        stateService.markDeadByNonRetryableFailure(requestId, failReason);
        sendAttemptRecorder.record(request, result, NotificationStatus.DEAD);

        log.debug("알림 재시도 불가능 - requestId={} reason={}", requestId, failReason);
    }

    private NotificationStatus handleRetryableFailure(NotificationRequestDto request, Long requestId, String failReason) {
        int nextRetrySeconds = retryProperties.nextRetrySeconds();
        int maxRetryCount = retryProperties.maxRetryCount();
        int nextRetryCount = request.getRetryCount() + 1;

        stateService.markRetryableFailure(requestId, failReason, nextRetrySeconds, maxRetryCount);

        if (nextRetryCount >= maxRetryCount) {
            stateService.markDeadAfterRetryExceeded(requestId, failReason, maxRetryCount);

            log.debug("알림 최대 재시도 초과로 DEAD 처리 - requestId={} reason={} maxRetryCount={}", requestId, failReason, maxRetryCount);

            return NotificationStatus.DEAD;
        }

        log.debug("알림 재시도 가능 - requestId={} reason={} nextRetrySeconds={} maxRetryCount={}",
                requestId, failReason, nextRetrySeconds, maxRetryCount);

        return NotificationStatus.FAILED;
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
