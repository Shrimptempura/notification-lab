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
public class CampaignSummaryDto {

    private Long summaryId;
    private Long campaignId;

    private Long totalRequestCount;         // request에서 가져옴
    private Long totalAttemptCount;         // attempt에서 가져옴

    private Long sentCount;     // request에서 가져옴
    private Long deadCount;     // request에서 가져옴

    private LocalDateTime processedStartedAt;       // request.created_at MIN
    private LocalDateTime processedFinishedAt;      // request.updated_at MAX

    private Long durationSeconds;
}
