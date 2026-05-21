package portfolio.notification_lab;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class NotificationRequestStateMapperTestSupport {

    private final JdbcTemplate jdbcTemplate;

    // 상태와 재시도 횟수를 지정하여 요청을 삽입하는 헬퍼 메서드
    public Long insertRequest(String status, int retryCount) {
        return jdbcTemplate.queryForObject("""
                INSERT INTO notification_request (
                    campaign_id,
                    recipient_id,
                    status,
                    payload,
                    retry_count,
                    created_at,
                    updated_at
                ) VALUES (
                    1,
                    1001,
                    ?,
                    'test payload',
                    ?,
                    NOW(),
                    NOW()
                )
                RETURNING id
                """, Long.class, status, retryCount);
    }

    // FAILED 상태 전용 테스트 데이터
    // retry_count, next_retry_at, fail_reason, failed_at 필요
    public Long insertFailedRequest(int retryCount, LocalDateTime nextRetryAt) {
        return jdbcTemplate.queryForObject("""
                INSERT INTO notification_request (
                    campaign_id,
                    recipient_id,
                    status,
                    payload,
                    retry_count,
                    fail_reason,
                    failed_at,
                    next_retry_at,
                    created_at,
                    updated_at
                ) VALUES (
                    1,
                    1001,
                    'FAILED',
                    'test payload',
                    ?,
                    'TEMPORARY_PROVIDER_ERROR',
                    NOW(),
                    ?,
                    NOW(),
                    NOW()
                )
                RETURNING id
                """, Long.class, retryCount, Timestamp.valueOf(nextRetryAt));
    }

    // RESERVED 상태에서 PENDING 복구 테스트 데이터
    // 복구할려면 reserved_at이 중요
    public Long insertReservedRequest(LocalDateTime reservedAt) {
        return jdbcTemplate.queryForObject("""
                INSERT INTO notification_request (
                    campaign_id,
                    recipient_id,
                    status,
                    payload,
                    retry_count,
                    reserved_at,
                    created_at,
                    updated_at
                ) VALUES (
                    1,
                    1001,
                    'RESERVED',
                    'test payload',
                    0,
                    ?,
                    NOW(),
                    NOW()
                )
                RETURNING id
                """, Long.class, Timestamp.valueOf(reservedAt));
    }

    public Map<String, Object> findRequestById(Long requestId) {
        return jdbcTemplate.queryForMap("""
                SELECT *
                FROM notification_request
                WHERE id = ?
                """, requestId);
    }
}
