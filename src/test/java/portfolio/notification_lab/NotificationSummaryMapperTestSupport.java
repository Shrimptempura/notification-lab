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

    public Long insertRequest(Long campaignId, Long recipientId, String status) {
        return jdbcTemplate.queryForObject("""
                INSERT INTO notification_request (
                    campaign_id,
                    recipient_id,
                    status,
                    payload,
                    retry_count,
                    created_at
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

}
