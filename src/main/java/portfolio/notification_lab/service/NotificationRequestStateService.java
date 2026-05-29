package portfolio.notification_lab.service;

public interface NotificationRequestStateService {

    // PENDING -> RESERVED
    void reserve(Long requestId);

    // RESERVED -> SENT
    void markSent(Long requestId);

    // RESERVED -> FAILED
    void markRetryableFailure(Long requestId, String failReason, int nextRetrySeconds, int maxRetryCount);

    // FAILED -> PENDING
    void markReadyForRetry(Long requestId);

    // FAILED -> DEAD
    void markDeadAfterRetryExceeded(Long requestId, String failReason);

    // RESERVED -> DEAD
    void markDeadByNonRetryableFailure(Long requestId, String failReason);

    // RESERVED -> PENDING, 복구 시간
    // reserved_at &lt; NOW() - (#{timeoutMinutes} * INTERVAL '1 minute') 기준
    // 방치된 RESERVED 선점 PENDING 상태로 변환
    int releaseExpiredReservations(int timeoutMinutes);
}
