package portfolio.notification_lab.service;

import portfolio.notification_lab.dto.NotificationStatusCountDto;

import java.util.List;

public interface NotificationMetricsService {

    List<NotificationStatusCountDto> countByStatus();
}
