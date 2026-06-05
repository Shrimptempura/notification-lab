package portfolio.notification_lab;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class NotificationWorkerMapperTestSupport {

    private final JdbcTemplate jdbcTemplate;

    public Long insertRequest(String status,
                              int retryCount,
                              Integer nextRetryMinutes,
                              Integer reservedMinutes,
                              int createdMinutesAgo
    ) {
        LocalDateTime now = LocalDateTime.now();

        LocalDateTime nextRetryAt = nextRetryMinutes == null ? null : now.plusMinutes(nextRetryMinutes);
        LocalDateTime reservedAt = reservedMinutes == null ? null : now.plusMinutes(reservedMinutes);

        return jdbcTemplate.queryForObject("""
                INSERT INTO notification_request (
                    campaign_id,
                    recipient_id,
                    status,
                    payload,
                    retry_count,
                    next_retry_at,
                    reserved_at,
                    created_at,
                    updated_at
                ) VALUES (
                    1,
                    1,
                    ?,
                    'test payload',
                    ?,
                    ?,
                    ?,
                    NOW() - (? * INTERVAL '1 minute'),
                    NOW()
                ) RETURNING id
                """,
                Long.class,
                status,
                retryCount,
                nextRetryAt,
                reservedAt,
                createdMinutesAgo
        );
    }

    public Map<String, Object> findRequestById(Long requestId) {
        return jdbcTemplate.queryForMap("""
                SELECT
                    id,
                    campaign_id,
                    recipient_id,
                    status,
                    payload,
                    retry_count,
                    fail_reason,
                    next_retry_at,
                    reserved_at,
                    sent_at,
                    failed_at,
                    dead_at,
                    created_at,
                    updated_at
                FROM notification_request
                WHERE id = ?
                """, requestId);
    }

    public Map<String, Object> findSendAttemptByRequestId(Long requestId) {
        return jdbcTemplate.queryForMap("""
                SELECT
                    id,
                    request_id,
                    campaign_id,
                    recipient_id,
                    attempt_no,
                    result_status,
                    provider_result_type,
                    fail_reason,
                    created_at
                FROM notification_send_attempt
                WHERE request_id = ?
                """, requestId);
    }
}
