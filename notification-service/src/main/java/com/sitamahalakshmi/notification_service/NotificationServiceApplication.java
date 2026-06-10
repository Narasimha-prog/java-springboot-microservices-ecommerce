package com.sitamahalakshmi.notification_service;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.MapPropertySource;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@SpringBootApplication
public class NotificationServiceApplication {

	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(NotificationServiceApplication.class);
		app.addInitializers(ctx -> {
			// 1. Configure Dotenv safely to prevent classpath file execution panic loops inside containers
			Dotenv dotenv = Dotenv.configure()
					.ignoreIfMalformed()
					.ignoreIfMissing()
					.load();

			// 2. Resolution strategy tree: Look at the Linux System Environment context first, fallback to the file asset second
			String appPassword = System.getenv("app_password") != null ? System.getenv("app_password") : dotenv.get("app_password");

			Map<String, Object> properties = new HashMap<>();

			if (appPassword != null) {
				properties.put("app-password", appPassword);
			} else {
				// Safety alert fallback log for debugging missing variable assignments
				System.out.println("⚠️ WARNING: JWT 'SECRET' property could not be resolved from Environment or local file!");
			}

			// 3. Inject variables cleanly into Spring context configuration environment
			if (!properties.isEmpty()) {
				ctx.getEnvironment()
						.getPropertySources()
						.addFirst(new MapPropertySource("dotenvProperties", properties));
			}
		});

		app.run(args);
	}

}
