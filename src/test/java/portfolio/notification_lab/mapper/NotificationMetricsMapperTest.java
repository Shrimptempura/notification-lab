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
import portfolio.notification_lab.NotificationWorkerMapperTestSupport;
import portfolio.notification_lab.dto.NotificationStatusCountDto;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
public class NotificationMetricsMapperTest {

    @Autowired
    private NotificationMetricsMapper mapper;

    @Autowired
    private NotificationWorkerMapperTestSupport support;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("TRUNCATE TABLE notification_request RESTART IDENTITY");
    }

    @Test
    @DisplayName("상태별 요청수 조회")
    void countByStatus_success() {
        support.insertRequest("PENDING", 0, null, null, 10);
        support.insertRequest("PENDING", 0, null, null, 20);
        support.insertRequest("RESERVED", 0, null, null, 20);
        support.insertRequest("FAILED", 1, null, null, 30);
        support.insertRequest("SENT", 0, null, null, 40);

        List<NotificationStatusCountDto> result = mapper.countByStatus();

        Map<String, Long> countMap = result.stream()
                .collect(Collectors.toMap(
                        NotificationStatusCountDto::getStatus, NotificationStatusCountDto::getCount));

        assertThat(countMap.get("PENDING")).isEqualTo(2L);
        assertThat(countMap.get("RESERVED")).isEqualTo(1L);
        assertThat(countMap.get("FAILED")).isEqualTo(1L);
        assertThat(countMap.get("SENT")).isEqualTo(1L);
    }
}
