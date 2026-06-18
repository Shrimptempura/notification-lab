package portfolio.notification_lab.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import portfolio.notification_lab.NotificationSummaryMapperTestSupport;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
class NotificationCampaignSummaryMapperTest {

    @Autowired
    private NotificationCampaignSummaryMapper mapper;

    @Autowired
    private NotificationSummaryMapperTestSupport support;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("""
                TRUNCATE TABLE
                    notification_campaign_summary, 
                    notification_send_attempt,
                    notification_request 
                    RESTART IDENTITY""");
    }

    @Test
    @DisplayName("request 테이블이 처리중이면 true 반환")
    void hasRemainingRequests_success() {

    }

    @Test
    @DisplayName("request 테이블 요청이 SENT 또는 DEAD이면 false 반환")
    void hasRemainingRequests_false() {

    }

    @Test
    @DisplayName("request 테이블에서 필요한 값 계산")
    void calculateRequests_success() {

    }

    @Test
    @DisplayName("attempt 테이블에서 총 발송 시도 수 계산")
    void countTotalAttempts_success() {

    }

    @Test
    @DisplayName("campaign summary 저장하고 campaignId로 단건 조회")
    void insertSummaryAndFindByCampaignId_success() {

    }

    @Test
    @DisplayName("전체 campaign summary 조회")
    void findAllSummaries_success() {

    }
}