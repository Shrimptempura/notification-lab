package portfolio.notification_lab.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import portfolio.notification_lab.dto.NotificationRequestDto;
import portfolio.notification_lab.dto.NotificationStatusCountDto;

import java.util.List;

@Mapper
public interface NotificationWorkerMapper {

    // PENDING 상태의 발송 대기 요청 조회
    // 최초 생성 + 복구된 PENDING 포함
    List<NotificationRequestDto> findPendingRequests(@Param("limit") int limit);

    // FAILED 알림에서 재시도 가능한 요청 조회
    // retryCount < maxRetryCount
    // nextRetryAt < NOW()
    List<NotificationRequestDto> findRetryableFailedRequests(@Param("limit") int limit,
                                                             @Param("maxRetryCount") int maxRetryCount);

    // 지금 worker가 처리 가능한 요청 조회
    // PENDING 상태의 발송 대기 요청
    // FAILED 중 재시도 시간 제한이 도달한 요청
    List<NotificationRequestDto> findRunnableRequests(@Param("limit") int limit,
                                                      @Param("maxRetryCount") int maxRetryCount);

    // 지금 worker가 처리 가능한 요청 선별하고 RESERVED로 점유한다
    // PENDING 또는 재시도 가능한 FAILED 요청 가져옴
    // 가져와서 RESERVED로 상태 업데이트
    // FAILED는 -> PENDING -> RESERVED를 거치지 않고 바로 RESERVED로 점유함(복구 가능한 조건의 FAILED)
    // for update skip locked 사용해서 worker 중복 점유 방지
    List<NotificationRequestDto> reserveRunnableRequests(@Param("limit") int limit,
                                                         @Param("maxRetryCount") int maxRetryCount);

    // RESERVED 상태로 오래 방치된 요청 조회
    // stateMapper의 releaseExpiredReservations과 연계해서 사용
    // releaseExpiredReservations: update, findExpiredReservations: select
    List<NotificationRequestDto> findExpiredReservations(@Param("timeoutMinutes") int timeoutMinutes);

    // 상태별 요청 수 조회
    List<NotificationStatusCountDto> countByStatus();
}