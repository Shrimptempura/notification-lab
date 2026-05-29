package portfolio.notification_lab.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "notification.worker")
public record NotificationWorkerProperties(
        boolean enabled,
        int limit,
        long fixedDelayMs
) {
}
