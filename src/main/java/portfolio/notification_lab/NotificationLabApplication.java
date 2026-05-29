package portfolio.notification_lab;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;
import portfolio.notification_lab.config.NotificationRecoveryProperties;
import portfolio.notification_lab.config.NotificationRetryProperties;
import portfolio.notification_lab.config.NotificationWorkerProperties;

@EnableScheduling
@ConfigurationPropertiesScan
@SpringBootApplication
public class NotificationLabApplication {

	public static void main(String[] args) {
		SpringApplication.run(NotificationLabApplication.class, args);
	}

}
