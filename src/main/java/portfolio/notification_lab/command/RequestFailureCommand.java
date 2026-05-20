package portfolio.notification_lab.command;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RequestFailureCommand {

    private final Long requestId;
    private final String failReason;
    private final int nextRetrySeconds;   // failed된 메시지 재시도 시간(reserved 고장난거 worker 장애 복구가 아님)
}
