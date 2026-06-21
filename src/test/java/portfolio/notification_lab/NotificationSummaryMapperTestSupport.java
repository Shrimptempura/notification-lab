package portfolio.notification_lab;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationSummaryMapperTestSupport {

    @Autowired
    private final JdbcTemplate jdbcTemplate;

    // 기존꺼는 recipientId가 고정
    public Long insertRequest(Long campaignId, Long recipientId, String status) {
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
                    ?,
                    ?,
                    ?,
                    'test payload',
                    0,
                    NOW(),
                    NOW()
                )
                RETURNING id
                """, Long.class, campaignId, recipientId, status);
    }

    // summary 테스트용 attempt 생성
    public void insertSendAttempt(Long requestId, Long campaignId, Long recipientId, int attemptNo, String resultStatus, String providerResultType) {
        jdbcTemplate.update("""
                INSERT INTO notification_send_attempt (
                    request_id,
                    campaign_id,
                    recipient_id,
                    attempt_no,
                    result_status,
                    provider_result_type,
                    fail_reason,
                    created_at
                ) VALUES (
                    ?,
                    ?,
                    ?,
                    ?,
                    ?,
                    ?,
                    'test fail reason',
                    NOW()
                )
                """, requestId, campaignId, recipientId, attemptNo, resultStatus, providerResultType);
    }
}
