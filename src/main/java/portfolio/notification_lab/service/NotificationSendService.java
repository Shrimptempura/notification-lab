package portfolio.notification_lab.service;

import portfolio.notification_lab.dto.NotificationRequestDto;

// RESERVED 상태 요청을 실제 발송 흐름으로 처리하는 서비스
// NotificationProvider를 통해 발송을 시도한다
// NotificationProvider가 반환한 SendResult를 해석후
// 결과에 따라 SENT/FAILED/DEAD 처리함
public interface NotificationSendService {

    // RESERVED 상태인 알림 요청을 1건 처리
    // 발송 성공 -> SENT
    // 재시도 가능한 실패 -> FAILED
    // 재시도 불가능한 실패 -> DEAD
    void sendOne(NotificationRequestDto request);
}
