package portfolio.notification_lab.mapper;

import org.apache.ibatis.annotations.Mapper;
import portfolio.notification_lab.dto.NotificationStatusCountDto;

import java.util.List;

@Mapper
public interface NotificationMetricsMapper {

    // 상태별 요청 수 조회
    List<NotificationStatusCountDto> countByStatus();
}
