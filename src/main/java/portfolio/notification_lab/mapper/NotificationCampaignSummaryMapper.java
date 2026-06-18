package portfolio.notification_lab.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import portfolio.notification_lab.dto.CampaignSummaryDto;

import java.util.List;

@Mapper
public interface NotificationCampaignSummaryMapper {

    // summary 생성 전, request와 attempt 테이블 종료 확인
    boolean hasRemainingRequests(@Param("campaignId") Long campaignId);

    // request에서 계산할 total/sent/dead/time을 가져옴
    CampaignSummaryDto calculateRequests(@Param("campaignId") Long campaignId);

    // attempt에서 총 발송 시도 가져옴
    long countTotalAttempts(@Param("campaignId") Long campaignId);

    // summary 저장
    int insertSummary(CampaignSummaryDto summary);

    // 단건 조회
    CampaignSummaryDto findSummaryByCampaignId(@Param("campaignId") Long campaignId);

    // 전체 조회
    List<CampaignSummaryDto> findAllSummaries();
}
