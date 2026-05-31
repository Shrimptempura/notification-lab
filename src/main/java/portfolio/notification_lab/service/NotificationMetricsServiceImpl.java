package portfolio.notification_lab.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import portfolio.notification_lab.dto.NotificationStatusCountDto;
import portfolio.notification_lab.mapper.NotificationMetricsMapper;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationMetricsServiceImpl implements NotificationMetricsService {

    private final NotificationMetricsMapper metricsMapper;

    // 현재 상태별 요청 수 조회
    @Transactional(readOnly = true)
    @Override
    public List<NotificationStatusCountDto> countByStatus() {
        List<NotificationStatusCountDto> statusCount = metricsMapper.countByStatus();

        log.debug("현재 상태별 요청 개수 조회 완료");

        return statusCount;
    }
}
