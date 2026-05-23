package portfolio.notification_lab.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import portfolio.notification_lab.command.RequestDeadCommand;
import portfolio.notification_lab.command.RequestFailureCommand;
import portfolio.notification_lab.mapper.NotificationRequestStateMapper;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class NotificationRequestStateServiceImpl implements NotificationRequestStateService {

    private final NotificationRequestStateMapper mapper;

    // PENDING -> RESERVED
    // worker가 발송 요청을 선점한다
    @Override
    public void reserve(Long requestId) {
        validateRequestId(requestId);

        int updated = mapper.markPendingAsReserved(requestId);

        if (updated != 1) {
            log.warn("알림 요청 선점 실패 - PENDING 상태가 아니거나 이미 처리됨. requestId={}", requestId);
            throw new IllegalStateException("알림 요청 선점 실패");
        }

        log.debug("알림 요청 선점 완료 - requestId={}", requestId);
    }

    // RESERVED -> SENT
    // 발송 성공 결과를 반영한다.
    @Override
    public void markSent(Long requestId) {
        validateRequestId(requestId);

        int updated = mapper.markReservedAsSent(requestId);

        if (updated != 1) {
            log.warn("알림 SENT 처리 실패 - RESERVED 상태가 아님. requestId={}", requestId);
            throw new IllegalStateException("알림 SENT 처리 실패");
        }

        log.debug("알림 요청 SENT 처리 완료 - requestId={}", requestId);
    }

    // RESERVED -> FAILED
    // 실패를 기록하고 다음 재시도 시간을 설정한다
    @Override
    public void markRetryableFailure(Long requestId, String failReason, int nextRetrySeconds, int maxRetryCount) {
        validateRequestId(requestId);
        validateFailReason(failReason);
        validateNextRetrySeconds(nextRetrySeconds);
        validateMaxRetryCount(maxRetryCount);

        RequestFailureCommand command = new RequestFailureCommand(requestId, failReason, nextRetrySeconds, maxRetryCount);

        int updated = mapper.markReservedAsFailed(command);

        if (updated != 1) {
            log.warn("알림 FAILED 처리 실패 - RESERVED 상태가 아니거나 최대 재시도 횟수에 도달함. requestId={} failReason={} maxRetryCount={}", requestId, failReason, maxRetryCount);
            throw new IllegalStateException("알림 FAILED 처리 실패");
        }

        log.debug("알림 FAILED 처리 완료 - requestId={} failReason={} nextRetrySeconds={} maxRetryCount={}", requestId, failReason, nextRetrySeconds, maxRetryCount);
    }

    // FAILED -> PENDING
    // 재시도 시간이 되었고 retry_count가 허용범위인 경우 다시 요청을 대기 상태(PENDING)으로 돌린다.
    @Override
    public void markReadyForRetry(Long requestId) {
        validateRequestId(requestId);

        int updated = mapper.markFailedAsPending(requestId);

        if (updated != 1) {
            log.warn("알림 재시도 대기 전환 실패 - 재시도 조건 미충족. requestId={}", requestId);
            throw new IllegalStateException("알림 재시도 대기 전환 실패");
        }

        log.debug("알림 재시도 대기 전환 완료 - requestId={}", requestId);
    }

    // FAILED -> DEAD
    // 최대 재시도 횟수에 도달하면 요청을 최종 실패 처리한다.
    @Override
    public void markDeadAfterRetryExceeded(Long requestId, String failReason) {
        validateRequestId(requestId);
        validateFailReason(failReason);

        RequestDeadCommand command = new RequestDeadCommand(requestId, failReason);

        int updated = mapper.markFailedAsDead(command);

        if (updated != 1) {
            log.warn("알림 DEAD 처리 실패 - FAILED 상태가 아니거나 retry_count 조건 미충족. requestId={} failReason={}", requestId, failReason);
            throw new IllegalStateException("알림 DEAD 처리 실패");
        }

        log.debug("알림 DEAD 처리 완료 - 최대 재시도 초과 requestId={} failReason={}", requestId, failReason);
    }

    // RESERVED -> DEAD
    // 재시도 불가능한 실패를 반환한 경우 즉시 최종 실패 처리 한다.
    @Override
    public void markDeadByNonRetryableFailure(Long requestId, String failReason) {
        validateRequestId(requestId);
        validateFailReason(failReason);

        RequestDeadCommand command = new RequestDeadCommand(requestId, failReason);

        int updated = mapper.markReservedAsDead(command);

        if (updated != 1) {
            log.warn("알림 DEAD 처리 실패 - RESERVED 상태 아님. requestId={} failReason={}", requestId, failReason);
            throw new IllegalStateException("알림 DEAD 처리 실패");
        }

        log.debug("알림 DEAD 처리 완료 - 재시도 불가능 실패. requestId={} failReason={}", requestId, failReason);
    }

    // RESERVED -> PENDING
    // worker 장애 등으로 오래 묶인 RESERVED 요청을 다시 대기 상태로 복구한다. (retry_count 증가 X)
    @Override
    public int releaseExpiredReservations(int timeoutMinutes) {
        validateTimeoutMinutes(timeoutMinutes);

        int releasedCount = mapper.releaseExpiredReservations(timeoutMinutes);

        log.info("만료된 알림 선점 복구 완료. timeoutMinutes={} releasedCount={}", timeoutMinutes, releasedCount);

        return releasedCount;
    }

    // ------- helper methods -------
    private void validateRequestId(Long requestId) {
        if (requestId == null || requestId <= 0) {
            throw new IllegalArgumentException("requestId가 유효하지 않습니다: " + requestId);
        }
    }

    private void validateFailReason(String failReason) {
        if (failReason == null || failReason.isBlank()) {
            throw new IllegalArgumentException("failReason은 비어 있을 수 없습니다.");
        }
    }

    private void validateNextRetrySeconds(int nextRetrySeconds) {
        if (nextRetrySeconds <= 0) {
            throw new IllegalArgumentException("nextRetrySeconds는 1 이상이어야 합니다: " + nextRetrySeconds);
        }
    }

    private void validateTimeoutMinutes(int timeoutMinutes) {
        if (timeoutMinutes <= 0) {
            throw new IllegalArgumentException("timeoutMinutes는 1 이상이어야 합니다: " + timeoutMinutes);
        }
    }

    private void validateMaxRetryCount(int maxRetryCount) {
        if (maxRetryCount < 1) {
            throw new IllegalArgumentException("maxRetryCount는 1 이상이어야 합니다: " + maxRetryCount);
        }
    }





}
