package com.srinu.chatbot_service;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.MapPropertySource;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@SpringBootApplication
@EnableJpaAuditing
public class ChatbotServiceApplication {

	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(ChatbotServiceApplication.class);

		// Add initializer to inject .env values into Spring Environment
		app.addInitializers( ctx -> {
			Dotenv dotenv = Dotenv.load();

			Map<String, Object> properties = new HashMap<>();
			properties.put("DB_URL", Objects.requireNonNull(dotenv.get("DB_URL")));
			properties.put("DB_USER_NAME", Objects.requireNonNull(dotenv.get("DB_USER_NAME")));
			properties.put("DB_PASSWORD", Objects.requireNonNull(dotenv.get("DB_PASSWORD")));

			// Add as first property source so it overrides other defaults
			ctx.getEnvironment()
					.getPropertySources()
					.addFirst(new MapPropertySource("dotenvProperties", properties));
		});

		app.run(args);
	}

}
