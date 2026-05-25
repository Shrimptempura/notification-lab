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
        Long thirdId = support.insertRequest("PENDING", 0, null, null, 10);

        List<NotificationRequestDto> result = mapper.findPendingRequests(2);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(NotificationRequestDto::getId)
                .containsExactly(firstId, secondId);
    }

}