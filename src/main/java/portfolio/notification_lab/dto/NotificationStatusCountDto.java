package portfolio.notification_lab.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class NotificationStatusCountDto {

    private String status;
    private long count;
}
