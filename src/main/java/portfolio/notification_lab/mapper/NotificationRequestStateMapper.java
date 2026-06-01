package portfolio.notification_lab.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import portfolio.notification_lab.command.RequestDeadCommand;
import portfolio.notification_lab.command.RequestFailureCommand;

@Mapper
public interface NotificationRequestStateMapper {

    // PENDING -> RESERVED
    int markPendingAsReserved(@Param("requestId") Long requestId);

    // RESERVED -> SENT
    int markReservedAsSent(@Param("requestId") Long requestId);

    // RESERVED -> FAILED
    int markReservedAsFailed(RequestFailureCommand command);

    // FAILED -> PENDING
    int markFailedAsPending(@Param("requestId") Long requestId,
                            @Param("maxRetryCount") int maxRetryCount);

    // FAILED -> DEAD
    int markFailedAsDead(@Param("requestId") Long requestId,
                         @Param("failReason") String failReason,
                         @Param("maxRetryCount") int maxRetryCount);

    // RESERVED -> DEAD
    int markReservedAsDead(RequestDeadCommand command);

    // RESERVED -> PENDING, 복구 시간
    // reserved_at &lt; NOW() - (#{timeoutMinutes} * INTERVAL '1 minute') 기준
    // 방치된 RESERVED 선점 PENDING 상태로 변환
    int releaseExpiredReservations(@Param("timeoutMinutes") int timeoutMinutes);


}
