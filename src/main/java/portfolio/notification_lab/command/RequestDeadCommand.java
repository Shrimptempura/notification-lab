package portfolio.notification_lab.command;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RequestDeadCommand {

    private final Long requestId;
    private final String failReason;
}
