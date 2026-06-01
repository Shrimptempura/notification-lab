-- 인덱스
CREATE INDEX idx_notification_pending_order
    ON notification_request (created_at, id)
    WHERE status = 'PENDING';

CREATE INDEX idx_notification_failed_retry
    ON notification_request (next_retry_at, id)
    WHERE status = 'FAILED';