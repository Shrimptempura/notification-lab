package portfolio.notification_lab.mapper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
class NotificationRequestMapperTest {

    @Autowired
    private NotificationRequestMapper mapper;

    @Test
    @DisplayName("전체 요청 개수 조회")
    void countAll() {
        int count = mapper.countAll();
        assertThat(count).isGreaterThanOrEqualTo(0);
    }

    @Test
    @DisplayName("상태별 요청 개수 조회")
    void countByStatus() {
        int count = mapper.countByStatus("PENDING");
        assertThat(count).isGreaterThanOrEqualTo(0);
    }

    @Test
    @DisplayName("모든 요청을 PENDING으로 초기화")
    void resetAllToPending() {
        int updatedCount = mapper.resetAllToPending();
        assertThat(updatedCount).isGreaterThanOrEqualTo(0);
    }

}