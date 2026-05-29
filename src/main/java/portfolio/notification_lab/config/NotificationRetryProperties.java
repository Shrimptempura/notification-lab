package portfolio.notification_lab.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "notification.retry")
public record NotificationRetryProperties(
        int nextRetrySeconds,
        int maxRetryCount
) {
}
