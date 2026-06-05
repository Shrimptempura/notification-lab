package portfolio.notification_lab.recorder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import portfolio.notification_lab.command.SendAttemptCommand;
import portfolio.notification_lab.domain.notification.NotificationStatus;
import portfolio.notification_lab.dto.NotificationRequestDto;
import portfolio.notification_lab.mapper.NotificationSendAttemptMapper;
import portfolio.notification_lab.provider.SendResult;

@Slf4j
@RequiredArgsConstructor
@Component
public class SendAttemptRecorderImpl implements SendAttemptRecorder {

    private final NotificationSendAttemptMapper sendAttemptMapper;

    @Override
    public void record(NotificationRequestDto request, SendResult result, NotificationStatus finalStatus) {
        validate(request, result, finalStatus);

        int attemptNo = request.getRetryCount() + 1;
        String providerResultType = getProviderResultType(result);

        SendAttemptCommand command = new SendAttemptCommand(
                request.getId(), request.getCampaignId(), request.getRecipientId(), attemptNo, finalStatus.name(), providerResultType, result.failReason());

        int inserted = sendAttemptMapper.insertSendAttempt(command);

        if (inserted != 1) {
            log.warn("발송 시도 기록 저장 실패 - requestId={}, attemptNo={}", request.getId(), attemptNo);
            throw new IllegalStateException("발송 시도 이력 저장 실패");
        }

        log.debug("발송 시도 기록 저장 성공 - requestId={}, attemptNo={}, resultStatus={}",
                request.getId(), attemptNo, finalStatus);
    }

    private String getProviderResultType(SendResult result) {
        if (result.success()) {
            return "SENT";
        }

        if (result.retryable()) {
            return "RETRYABLE_FAILURE";
        }

        return "NON_RETRYABLE_FAILURE";
    }

    private void validate(NotificationRequestDto request, SendResult sendResult, NotificationStatus finalStatus) {
        if (request == null) {
            throw new IllegalArgumentException("발송 정보는 필수입니다.");
        }

        if (request.getId() == null || request.getId() <= 0) {
            throw new IllegalArgumentException("유효한 requestId가 필요합니다. requestId=" + request.getId());
        }

        if (request.getRecipientId() == null || request.getRecipientId() <= 0) {
            throw new IllegalArgumentException("유효한 recipientId가 필요합니다. recipientId=" + request.getRecipientId());
        }

        if (request.getCampaignId() == null || request.getCampaignId() <= 0) {
            throw new IllegalArgumentException("유효한 campaignId가 필요합니다. campaignId=" + request.getCampaignId());
        }

        if (request.getRetryCount() < 0) {
            throw new IllegalArgumentException("재시도 횟수는 0 이상이어야 합니다. retryCount=" + request.getRetryCount());
        }

        if (sendResult == null) {
            throw new IllegalArgumentException("발송 결과는 필수입니다.");
        }

        if (finalStatus == null) {
            throw new IllegalArgumentException("최종 발송 상태는 필수입니다.");
        }
    }
}
