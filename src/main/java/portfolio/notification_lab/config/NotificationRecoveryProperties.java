package portfolio.notification_lab.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "notification.recovery")
public record NotificationRecoveryProperties(
        boolean enabled,
        int timeoutMinutes,
        long fixedDelayMs
) {
}
