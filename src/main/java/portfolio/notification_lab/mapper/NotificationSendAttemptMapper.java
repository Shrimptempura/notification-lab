package portfolio.notification_lab.mapper;

import org.apache.ibatis.annotations.Mapper;
import portfolio.notification_lab.command.SendAttemptCommand;

@Mapper
public interface NotificationSendAttemptMapper {

    int insertSendAttempt(SendAttemptCommand command);
}
