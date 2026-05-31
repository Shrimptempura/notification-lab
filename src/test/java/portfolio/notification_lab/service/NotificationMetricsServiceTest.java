package portfolio.notification_lab.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import portfolio.notification_lab.dto.NotificationStatusCountDto;
import portfolio.notification_lab.mapper.NotificationMetricsMapper;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class NotificationMetricsServiceTest {

    @Mock
    private NotificationMetricsMapper mapper;

    @InjectMocks
    private NotificationMetricsServiceImpl service;

    @Test
    @DisplayName("상태별 요청 수 조회")
    void countByStatus_success() {
        List<NotificationStatusCountDto> statusCounts = List.of(
                new NotificationStatusCountDto("PENDING", 10L),
                new NotificationStatusCountDto("RESERVED", 20L),
                new NotificationStatusCountDto("FAILED", 30L)
        );

        when(mapper.countByStatus()).thenReturn(statusCounts);

        List<NotificationStatusCountDto> result = service.countByStatus();

        assertThat(result).isSameAs(statusCounts);

        verify(mapper).countByStatus();
    }
}
