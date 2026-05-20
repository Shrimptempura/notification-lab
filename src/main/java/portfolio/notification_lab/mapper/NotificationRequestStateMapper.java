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
    int markFailedAsPending(@Param("requestId") Long requestId);

    // FAILED -> DEAD
    int markFailedAsDead(RequestDeadCommand command);

    // RESERVED -> DEAD
    int markReservedAsDead(RequestDeadCommand command);

    // RESERVED -> PENDING, 복구 시간
    int releaseExpiredReservations(@Param("timeoutMinutes") int timeoutMinutes);


}
