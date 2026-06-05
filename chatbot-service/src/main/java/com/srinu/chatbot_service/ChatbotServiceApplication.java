package com.srinu.chatbot_service;

import io.github.cdimascio.dotenv.Dotenv;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.MapPropertySource;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@SpringBootApplication
@EnableJpaAuditing
@OpenAPIDefinition(
		info = @Info(title = "ChatBot Service API", version = "1.0.0"),
		servers = {
				@Server(
						description = "API Gateway",
						url = "http://localhost:8080"
				),
				@Server(
						description = "API Inventory Service",
						url = "http://localhost:9999"
				)
		}
)

public class ChatbotServiceApplication {

	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(ChatbotServiceApplication.class);

		app.addInitializers(ctx -> {
			Map<String, Object> properties = new HashMap<>();

			// 1. Try to read directly from standard OS environment variables (Docker mode)
			String dbUrl = System.getenv("DB_URL");
			String dbUser = System.getenv("DB_USER_NAME");
			String dbPass = System.getenv("DB_PASSWORD");

			// 2. Fall back to reading the file ONLY if system environment variables are missing (IDE mode)
			if (dbUrl == null || dbUser == null || dbPass == null) {
				try {
					Dotenv dotenv = Dotenv.configure().ignoreIfMalformed().ignoreIfMissing().load();
					dbUrl = dotenv.get("DB_URL");
					dbUser = dotenv.get("DB_USER_NAME");
					dbPass = dotenv.get("DB_PASSWORD");
				} catch (Exception e) {
					System.out.println("⚠️ Notification Service: No local .env file found on disk.");
				}
			}

			// 3. Inject variables cleanly into Spring environment context if found
			if (dbUrl != null) properties.put("DB_URL", dbUrl);
			if (dbUser != null) properties.put("DB_USER_NAME", dbUser);
			if (dbPass != null) properties.put("DB_PASSWORD", dbPass);

			ctx.getEnvironment()
					.getPropertySources()
					.addFirst(new MapPropertySource("dotenvProperties", properties));
		});

		app.run(args);
	}

}
