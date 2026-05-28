package portfolio.notification_lab.provider;

import portfolio.notification_lab.dto.NotificationRequestDto;

// DB 상태를 직접 변경 X
// RESERVED, FAILED, SENT, DEAD는 다른 서비스에서 관리
public interface NotificationProvider {

    SendResult send(NotificationRequestDto request);
}
