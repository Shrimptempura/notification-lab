package portfolio.notification_lab.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import portfolio.notification_lab.dto.NotificationRequestDto;
import portfolio.notification_lab.mapper.NotificationWorkerMapper;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationWorkerServiceImpl implements NotificationWorkerService {

    private final NotificationWorkerMapper workerMapper;

    // PENDING 요청과 재시도 가능한 FAILED 요청을 RESERVED 상태로 선점하고 반환
    @Transactional
    @Override
    public List<NotificationRequestDto> reserveRunnableRequests(int limit, int maxRetryCount) {
        validateLimit(limit);
        validateMaxRetryCount(maxRetryCount);

        List<NotificationRequestDto> reservedRequests = workerMapper.reserveRunnableRequests(limit, maxRetryCount);

        log.debug("RESERVED로 처리 가능한 요청 선점 완료 - limit={} maxRetryCount={} reservedCount={}", limit, maxRetryCount, reservedRequests.size());

        return reservedRequests;
    }

    // ------------ helper --------------
    private void validateLimit(int limit) {
        if (limit <= 0) {
            throw new IllegalArgumentException("limit는 1 이상이어야 합니다." + limit);
        }
    }

    private void validateMaxRetryCount(int maxRetryCount) {
        if (maxRetryCount < 0) {
            throw new IllegalArgumentException("maxRetryCount는 0 이상이어야 합니다." + maxRetryCount);
        }
    }
}
