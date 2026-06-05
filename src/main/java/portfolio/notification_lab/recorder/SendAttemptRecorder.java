package portfolio.notification_lab.recorder;

import portfolio.notification_lab.domain.notification.NotificationStatus;
import portfolio.notification_lab.dto.NotificationRequestDto;
import portfolio.notification_lab.provider.SendResult;

public interface SendAttemptRecorder {

    void record(NotificationRequestDto request, SendResult result, NotificationStatus notificationStatus);
}
