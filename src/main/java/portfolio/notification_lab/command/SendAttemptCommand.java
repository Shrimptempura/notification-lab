package portfolio.notification_lab.command;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SendAttemptCommand {

    private final Long requestId;
    private final Long campaignId;
    private final Long recipientId;
    private final int attemptNo;
    private final String resultStatus;
    private final Boolean retryable;
    private final String failReason;
}
