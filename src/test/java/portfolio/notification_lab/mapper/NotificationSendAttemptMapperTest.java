package portfolio.notification_lab.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import portfolio.notification_lab.NotificationWorkerMapperTestSupport;
import portfolio.notification_lab.command.SendAttemptCommand;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class NotificationSendAttemptMapperTest {

    @Autowired
    NotificationSendAttemptMapper mapper;

    @Autowired
    NotificationWorkerMapperTestSupport support;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("""
            TRUNCATE TABLE notification_send_attempt, notification_request RESTART IDENTITY""");
    }

    @Test
    @DisplayName("발송 시도 이력 저장")
    void insertSendAttempt_success() {
        // FK 제약으로 requestId 필요
        Long requestId = support.insertRequest("FAILED", 0, null, null, 10);

        SendAttemptCommand command = new SendAttemptCommand(
                requestId, 10L, 100L, 1, "FAILED", true, "TIMEOUT");

        int result = mapper.insertSendAttempt(command);

        assertThat(result).isEqualTo(1);

        Map<String, Object> row = support.findSendAttemptByRequestId(requestId);
        
        assertThat(row.get("request_id")).isEqualTo(command.getRequestId());
        assertThat(row.get("campaign_id")).isEqualTo(command.getCampaignId());
        assertThat(row.get("recipient_id")).isEqualTo(command.getRecipientId());
        assertThat(row.get("attempt_no")).isEqualTo(command.getAttemptNo());
        assertThat(row.get("result_status")).isEqualTo(command.getResultStatus());
        assertThat(row.get("retryable")).isEqualTo(command.getRetryable());
        assertThat(row.get("fail_reason")).isEqualTo(command.getFailReason());
    }

    @Test
    @DisplayName("같은 요청의 같은 attempt_no는 중복 저장 불가 - (unique 확인)")
    void insertSendAttempt_duplicateAttemptNo_fail() {
        // FK 제약으로 requestId 필요
        Long requestId = support.insertRequest("FAILED", 0, null, null, 10);

        SendAttemptCommand command = new SendAttemptCommand(
                requestId, 10L, 100L, 1, "FAILED", true, "TIMEOUT");

        mapper.insertSendAttempt(command);

        assertThatThrownBy(() -> mapper.insertSendAttempt(command))
                .isInstanceOf(DuplicateKeyException.class);
    }
}