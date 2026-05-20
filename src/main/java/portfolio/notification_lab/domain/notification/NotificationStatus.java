package portfolio.notification_lab.domain.notification;

public enum NotificationStatus {
    PENDING,    // 발송 대기
    RESERVED,   // 선점
    SENT,       // 발송 성공
    FAILED,     // 실패(재시도 가능)
    DEAD        // 최종 실패(재시도 안함)
}
