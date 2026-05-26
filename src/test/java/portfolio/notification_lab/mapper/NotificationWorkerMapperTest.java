package portfolio.notification_lab.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import portfolio.notification_lab.NotificationWorkerMapperTestSupport;
import portfolio.notification_lab.dto.NotificationRequestDto;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
class NotificationWorkerMapperTest {

    @Autowired
    private NotificationWorkerMapper mapper;

    @Autowired
    private NotificationWorkerMapperTestSupport support;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("TRUNCATE TABLE notification_request RESTART IDENTITY");
    }

    @Test
    @DisplayName("PENDING 상태의 발송 대기 요청 조회")
    void findPendingRequests_success() {
        Long requestId = support.insertRequest("PENDING", 0, null, null, 10);
        support.insertRequest("RESERVED", 0, null, -10, 10);
        support.insertRequest("FAILED", 1, -10, null, 20);

        List<NotificationRequestDto> result = mapper.findPendingRequests(10);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(requestId);
        assertThat(result.get(0).getStatus()).isEqualTo("PENDING");
    }

    @Test
    @DisplayName("PENDING 상태 요청을 limit 만큼 조회한다")
    void findPendingRequests_limit() {
        Long firstId = support.insertRequest("PENDING", 0, null, null, 30);
        Long secondId = support.insertRequest("PENDING", 0, null, null, 20);
        support.insertRequest("PENDING", 0, null, null, 10);

        List<NotificationRequestDto> result = mapper.findPendingRequests(2);

        assertThat(result).hasSize(2);

        // 정렬기준: created_at ASC, id ASC
        assertThat(result).extracting(NotificationRequestDto::getId)
                .containsExactly(firstId, secondId);
    }

    @Test
    @DisplayName("재시도 가능한 FAILED 요청 조회")
    void findRetryableFailedRequests_success() {
        Long firstId = support.insertRequest("FAILED", 1, -20, null, 30);
        Long secondId = support.insertRequest("FAILED", 1, -10, null, 30);
        support.insertRequest("FAILED", 3, -10, null, 30);  // retry_count 초과
        support.insertRequest("FAILED", 1, 10, null, 40);   // next_retry_at 미래
        support.insertRequest("PENDING", 0, null, null, 10);    // status 제외

        List<NotificationRequestDto> result = mapper.findRetryableFailedRequests(10, 3);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(firstId);
        assertThat(result.get(0).getStatus()).isEqualTo("FAILED");

        // 정렬 기준: next_retry_at ASC, id ASC
        assertThat(result).extracting(NotificationRequestDto::getId)
                .containsExactly(firstId, secondId);
    }

    @Test
    @DisplayName("PENDING과 재시도 가능한 FAILED 요청을 함께 조회")
    void findRunnableRequests_success() {
        Long pendingId = support.insertRequest("PENDING", 0, null, null, 10);
        Long retryableFailedId = support.insertRequest("FAILED", 1, -10, null, 30);
        support.insertRequest("FAILED", 3, -10, null, 30);  // retry_count 초과
        support.insertRequest("FAILED", 1, 10, null, 40);   // next_retry_at 미래
        support.insertRequest("RESERVED", 0, null, -10, 30);  // RESERVED 제외

        List<NotificationRequestDto> result = mapper.findRunnableRequests(10, 3);

        assertThat(result).hasSize(2);

        // 정렬 기준: FAILED, PENDING, COALESCE(next_retry_at, created_at) ASC, id ASC
        assertThat(result).extracting(NotificationRequestDto::getId)
                .containsExactly(retryableFailedId, pendingId);

        assertThat(result).extracting(NotificationRequestDto::getStatus)
                .containsExactly("FAILED", "PENDING");
    }

    @Test
    @DisplayName("처리 가능한 요청을 RESERVED로 선점하고 반환")
    void reserveRunnableRequests_success() {
        Long pendingId = support.insertRequest("PENDING", 0, null, null, 30);   // 처리 가능 PENDING 상태
        Long retryableFailedId = support.insertRequest("FAILED", 1, -10, null, 30);// 재시도 가능한 FAILED
        support.insertRequest("FAILED", 3, -10, null, 30);  // retry_count 초과
        support.insertRequest("FAILED", 1, 10, null, 40);   // next_retry_at 미래
        support.insertRequest("SENT", 0, null, null, 30);   // 이미 완료된 요청

        List<NotificationRequestDto> result = mapper.reserveRunnableRequests(10, 3);

        assertThat(result).hasSize(2);

        // 선별만 정렬잡고 update은 안봄
        assertThat(result).extracting(NotificationRequestDto::getId)
                .containsExactlyInAnyOrder(pendingId, retryableFailedId);

        assertThat(result).extracting(NotificationRequestDto::getStatus)
                .containsOnly("RESERVED");

        assertThat(result).extracting(NotificationRequestDto::getReservedAt)
                .doesNotContainNull();
    }

    @Test
    @DisplayName("처리 가능한 요청을 limit 개수만큼만 RESERVED로 선점한다")
    void reserveRunnableRequests_limit() {
        Long firstId = support.insertRequest("PENDING", 0, null, null, 30);
        Long secondId = support.insertRequest("PENDING", 0, null, null, 20);
        Long thirdId = support.insertRequest("PENDING", 0, null, null, 10);

        List<NotificationRequestDto> result = mapper.reserveRunnableRequests(2, 3);

        assertThat(result).hasSize(2);

        assertThat(result).extracting(NotificationRequestDto::getId)
                .containsExactlyInAnyOrder(firstId, secondId);

        assertThat(result).extracting(NotificationRequestDto::getStatus)
                .containsOnly("RESERVED");

        assertThat(result).extracting(NotificationRequestDto::getReservedAt)
                .doesNotContainNull();

        Map<String, Object> thirdRow = support.findRequestById(thirdId);

        assertThat(thirdRow.get("status")).isEqualTo("PENDING");
        assertThat(thirdRow.get("reserved_at")).isNull();
    }

}