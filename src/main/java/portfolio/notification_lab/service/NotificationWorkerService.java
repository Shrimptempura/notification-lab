package portfolio.notification_lab.service;

import portfolio.notification_lab.dto.NotificationRequestDto;

import java.util.List;

public interface NotificationWorkerService {

    // PENDING, 재시도 가능 FAILED를 조회 후 바로 RESERVED 선점
    List<NotificationRequestDto> reserveRunnableRequests(int limit, int maxRetryCount);
}
