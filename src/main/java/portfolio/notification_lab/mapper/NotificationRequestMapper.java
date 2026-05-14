package portfolio.notification_lab.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface NotificationRequestMapper {

    // 전체 개수 확인
    int countAll();

    // 상태별 개수 확인
    int countByStatus(@Param("status") String status);

    // 전체 초기화(테스트)
    int resetAllToPending();


}
