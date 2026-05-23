package portfolio.notification_lab.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class NotificationRequestDto {

    private Long id;
    private Long campaignId;        // 이벤트 번호
    private Long recipientId;       // 수신자 번호
    private String status;
    private String payload;

    private int retryCount;
    private String failReason;

    private LocalDateTime nextRetryAt;
    private LocalDateTime reservedAt;
    private LocalDateTime sentAt;
    private LocalDateTime failedAt;
    private LocalDateTime deadAt;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
