package portfolio.notification_lab.controller;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import portfolio.notification_lab.dto.NotificationStatusCountDto;
import portfolio.notification_lab.service.NotificationMetricsService;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class NotificationMetricsController {

    private final NotificationMetricsService metricsService;

    @GetMapping("/notification/metrics/status-count")
    public List<NotificationStatusCountDto> countByStatus() {
        List<NotificationStatusCountDto> statusCounts = metricsService.countByStatus();
        log.debug("상태별 알림 요청 수 조회 API - statusCounts={}", statusCounts);

        return statusCounts;
    }
}
