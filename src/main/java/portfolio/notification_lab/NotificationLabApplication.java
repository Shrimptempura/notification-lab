package portfolio.notification_lab;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class NotificationLabApplication {

	public static void main(String[] args) {
		SpringApplication.run(NotificationLabApplication.class, args);
	}

}
