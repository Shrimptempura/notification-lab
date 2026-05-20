package portfolio.notification_lab.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface NotificationRequestStateMapper {

    // PENDING -> RESERVED
    int markPendingAsReserved(@Param("requestId") Long requestId);

    // RESERVED -> SENT
    int markReservedAsSent(@Param("requestId") Long requestId);

    // RESERVED -> FAILED
    int markReservedAsFailed(RequestFailureParam param);

    // FAILED -> PENDING
    int markFailedAsPending(@Param("requestId") Long requestId);

    // FAILED -> DEAD
    int markFailedAsDead(RequestDeadParam param);

    // RESERVED -> DEAD
    int markReservedAsDead(RequestDeadParam param);

    // RESERVED -> PENDING, 복구 시간
    int releaseExpiredReservations(@Param("timeoutMinutes") int timeoutMinutes);


}
