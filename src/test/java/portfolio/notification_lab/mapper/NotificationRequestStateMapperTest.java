package portfolio.notification_lab.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import portfolio.notification_lab.NotificationRequestStateMapperTestSupport;
import portfolio.notification_lab.command.RequestDeadCommand;
import portfolio.notification_lab.command.RequestFailureCommand;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
public class NotificationRequestStateMapperTest {

    @Autowired
    private NotificationRequestStateMapper mapper;

    @Autowired
    private NotificationRequestStateMapperTestSupport support;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("TRUNCATE TABLE notification_request RESTART IDENTITY");
    }

    @DisplayName("worker 선점 상태 전이")
    @Nested
    class ReservationTransition {

        @Test
        @DisplayName("PENDING 상태가 RESERVED로 선점")
        void markPendingAsReserved_success() {
            Long requestId = support.insertRequest("PENDING", 0);

            int updated = mapper.markPendingAsReserved(requestId);

            assertThat(updated).isEqualTo(1);

            Map<String, Object> row = support.findRequestById(requestId);
            assertThat(row.get("status")).isEqualTo("RESERVED");
            assertThat(row.get("reserved_at")).isNotNull();
        }

        @Test
        @DisplayName("PENDING 상태가 아니면 RESERVED로 선점되지 않는다")
        void markPendingAsReserved_fail_whenNotPending() {
            // PENDING 대신, SENT를 넣었음
            Long requestId = support.insertRequest("SENT", 0);

            int updated = mapper.markPendingAsReserved(requestId);

            assertThat(updated).isEqualTo(0);

            Map<String, Object> row = support.findRequestById(requestId);
            assertThat(row.get("status")).isEqualTo("SENT");
            assertThat(row.get("reserved_at")).isNull();
        }
    }

    @DisplayName("발송 결과 상태 전이")
    @Nested
    class SendResultTransition {

        @Test
        @DisplayName("RESERVED 상태가 SENT로 변경된다")
        void markReservedAsSent_success() {
            Long requestId = support.insertRequest("RESERVED", 0);

            int updated = mapper.markReservedAsSent(requestId);

            assertThat(updated).isEqualTo(1);

            Map<String, Object> row = support.findRequestById(requestId);
            assertThat(row.get("status")).isEqualTo("SENT");
            assertThat(row.get("sent_at")).isNotNull();
        }

        @Test
        @DisplayName("RESERVED 상태가 아니면 SENT로 변경되지 않는다")
        void markReservedAsSent_fail_whenNotReserved() {
            Long requestId = support.insertRequest("PENDING", 0);

            int updated = mapper.markReservedAsSent(requestId);

            assertThat(updated).isEqualTo(0);

            Map<String, Object> row = support.findRequestById(requestId);
            assertThat(row.get("status")).isEqualTo("PENDING");
            assertThat(row.get("sent_at")).isNull();
        }

        @Test
        @DisplayName("RESERVED 상태가 FAILED로 변경되고 retry_count가 증가한다")
        void markReservedAsFailed_success() {
            Long requestId = support.insertRequest("RESERVED", 0);
            RequestFailureCommand command = new RequestFailureCommand(requestId, "TEMPORARY_ERROR", 10, 3);

            int updated = mapper.markReservedAsFailed(command);

            assertThat(updated).isEqualTo(1);

            Map<String, Object> row = support.findRequestById(requestId);
            assertThat(row.get("status")).isEqualTo("FAILED");
            assertThat(row.get("retry_count")).isEqualTo(1);
            assertThat(row.get("fail_reason")).isEqualTo("TEMPORARY_ERROR");
            assertThat(row.get("failed_at")).isNotNull();
            assertThat(row.get("next_retry_at")).isNotNull();
        }

        @Test
        @DisplayName("RESERVED 상태가 아니면 FAILED로 변경되지 않는다")
        void markReservedAsFailed_fail_whenNotReserved() {
            Long requestId = support.insertRequest("PENDING", 0);
            RequestFailureCommand command = new RequestFailureCommand(requestId, "TEMPORARY_ERROR", 10, 3);

            int updated = mapper.markReservedAsFailed(command);

            assertThat(updated).isEqualTo(0);

            Map<String, Object> row = support.findRequestById(requestId);
            assertThat(row.get("status")).isEqualTo("PENDING");
            assertThat(row.get("retry_count")).isEqualTo(0);
            assertThat(row.get("fail_reason")).isNull();
            assertThat(row.get("failed_at")).isNull();
            assertThat(row.get("next_retry_at")).isNull();
        }

        @Test
        @DisplayName("retry_count가 최대 재시도 횟수 이상이라면 FAILED로 변경되지 않는다")
        void markReservedAsFailed_fail_whenRetryCountExceeded() {
            Long requestId = support.insertRequest("RESERVED", 3);
            RequestFailureCommand command = new RequestFailureCommand(requestId, "TEMPORARY_ERROR", 10, 3);

            int updated = mapper.markReservedAsFailed(command);

            assertThat(updated).isEqualTo(0);

            Map<String, Object> row = support.findRequestById(requestId);
            assertThat(row.get("status")).isEqualTo("RESERVED");
            assertThat(row.get("retry_count")).isEqualTo(3);
            assertThat(row.get("fail_reason")).isNull();
            assertThat(row.get("failed_at")).isNull();
            assertThat(row.get("next_retry_at")).isNull();
        }

        @Test
        @DisplayName("RESERVED 요청은 재시도 불가능한 실패 시 DEAD로 변경된다")
        void markReservedAsDead_success() {
            Long requestId = support.insertRequest("RESERVED", 0);
            RequestDeadCommand command = new RequestDeadCommand(requestId, "PERMANENT_ERROR");

            int updated = mapper.markReservedAsDead(command);

            assertThat(updated).isEqualTo(1);

            Map<String, Object> row = support.findRequestById(requestId);
            assertThat(row.get("status")).isEqualTo("DEAD");
            assertThat(row.get("fail_reason")).isEqualTo("PERMANENT_ERROR");
            assertThat(row.get("dead_at")).isNotNull();
        }
    }

    @DisplayName("retry 상태 전이")
    @Nested
    class RetryTransition {

        @Test
        @DisplayName("재시도 시간이 지난 FAILED 요청이 retry_count가 3 미만이면 PENDING으로 복구된다")
        void markFailedAsPending_success() {
            Long requestId = support.insertFailedRequest(2, LocalDateTime.now().minusSeconds(1));

            int updated = mapper.markFailedAsPending(requestId);

            assertThat(updated).isEqualTo(1);

            Map<String, Object> row = support.findRequestById(requestId);
            assertThat(row.get("status")).isEqualTo("PENDING");
            assertThat(row.get("retry_count")).isEqualTo(2);
        }

        @Test
        @DisplayName("재시도 시간이 지나지 않으면 FAILED 요청은 PENDING으로 복구되지 않는다")
        void markFailedAsPending_fail_whenRetryTimeNotReached() {
            Long requestId = support.insertFailedRequest(2, LocalDateTime.now().plusMinutes(10));

            int updated = mapper.markFailedAsPending(requestId);

            assertThat(updated).isEqualTo(0);

            Map<String, Object> row = support.findRequestById(requestId);
            LocalDateTime nextRetryAt = ((Timestamp) row.get("next_retry_at")).toLocalDateTime();

            assertThat(row.get("status")).isEqualTo("FAILED");
            assertThat(row.get("retry_count")).isEqualTo(2);
            assertThat(nextRetryAt).isAfter(LocalDateTime.now());
        }

        @Test
        @DisplayName("retry_count가 3이상이면 FAILED 요청은 PENDING으로 복구되지 않는다")
        void markFailedAsPending_fail_whenRetryCountExceeded() {
            Long requestId = support.insertFailedRequest(3, LocalDateTime.now().minusSeconds(1));

            int updated = mapper.markFailedAsPending(requestId);

            assertThat(updated).isEqualTo(0);

            Map<String, Object> row = support.findRequestById(requestId);
            assertThat(row.get("status")).isEqualTo("FAILED");
            assertThat(row.get("retry_count")).isEqualTo(3);
        }

        @Test
        @DisplayName("retry_count가 3이상인 FAILED 요청은 DEAD로 변경된다")
        void markFailedAsDead_success() {
            Long requestId = support.insertFailedRequest(3, LocalDateTime.now().minusSeconds(1));

            int updated = mapper.markFailedAsDead(requestId, "MAX_RETRY_EXCEEDED", 3);

            assertThat(updated).isEqualTo(1);

            Map<String, Object> row = support.findRequestById(requestId);
            assertThat(row.get("status")).isEqualTo("DEAD");
            assertThat(row.get("fail_reason")).isEqualTo("MAX_RETRY_EXCEEDED");
            assertThat(row.get("dead_at")).isNotNull();
        }

        @Test
        @DisplayName("retry_count가 3미만이면 FAILED 요청은 DEAD로 변경되지 않는다")
        void markFailedAsDead_fail_whenRetryCountNotReached() {
            Long requestId = support.insertFailedRequest(2, LocalDateTime.now().minusSeconds(1));

            int updated = mapper.markFailedAsDead(requestId, "MAX_RETRY_EXCEEDED", 3);

            assertThat(updated).isEqualTo(0);

            Map<String, Object> row = support.findRequestById(requestId);
            assertThat(row.get("status")).isEqualTo("FAILED");
            assertThat(row.get("retry_count")).isEqualTo(2);
            assertThat(row.get("dead_at")).isNull();
        }
    }

    @DisplayName("worker 장애 복구")
    @Nested
    class RecoveryTransition {

        @Test
        @DisplayName("오래된 RESERVED 요청은 PENDING으로 복구된다")
        void releaseExpiredReservations_success() {
            Long expireId = support.insertReservedRequest(LocalDateTime.now().minusMinutes(10));
            Long freshId = support.insertReservedRequest(LocalDateTime.now().plusMinutes(10));

            int updated = mapper.releaseExpiredReservations(5);

            System.out.println(support.findRequestById(expireId));
            System.out.println(support.findRequestById(freshId));

            assertThat(updated).isEqualTo(1);

            Map<String, Object> expireRow = support.findRequestById(expireId);
            Map<String, Object> freshRow = support.findRequestById(freshId);

            assertThat(expireRow.get("status")).isEqualTo("PENDING");
            assertThat(expireRow.get("reserved_at")).isNull();

            assertThat(freshRow.get("status")).isEqualTo("RESERVED");
            assertThat(freshRow.get("reserved_at")).isNotNull();
        }
    }
}