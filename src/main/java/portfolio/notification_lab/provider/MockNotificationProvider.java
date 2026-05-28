package portfolio.notification_lab.provider;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import portfolio.notification_lab.dto.NotificationRequestDto;

// 외부 API 없이 발송 성공/실패 재현
// recipientId 기준으로 실패 기준
// DB 상태 직접 변경 X, 실제 발송 X, 발송 결과만 SendResult로 반환
@Slf4j
@Component
public class MockNotificationProvider implements NotificationProvider{

    @Override
    public SendResult send(NotificationRequestDto request) {
        validateRequest(request);

        Long requestId = request.getId();
        Long recipientId = request.getRecipientId();

        // 데모용으로 recipientId 기준으로 20배는 DEAD, 6배는 FAILED
        if (recipientId % 20 == 0) {
            log.debug("Mock 발송 결과 - 재시도 불가능 실패. requestId={} recipientId={} reason={}", requestId, recipientId, "INVALID_RECIPIENT");

            return SendResult.nonRetryableFailure("INVALID_RECIPIENT");
        }

        if (recipientId % 6 == 0) {
            log.debug("Mock 발송 결과 - 재시도 가능 실패. requestId={} recipientId={} reason={}", requestId, recipientId, "TIMED_OUT");

            return SendResult.retryableFailure("TIMED_OUT");
        }

        log.debug("Mock 발송 결과 - 성공. requestId={} recipientId={}", requestId, recipientId);

        return SendResult.sent();
    }

    private void validateRequest(NotificationRequestDto request) {
        if (request == null) {
            throw new IllegalArgumentException("request는 null 일 수 없습니다.");
        }

        if (request.getRecipientId() == null || request.getRecipientId() <= 0) {
            throw new IllegalArgumentException("recipientId가 유효하지 않습니다: " + request.getRecipientId());
        }
    }
}
