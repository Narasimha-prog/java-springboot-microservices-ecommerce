package com.eswar.gatewayservice;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.MapPropertySource;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@SpringBootApplication
public class GatewayServiceApplication {

	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(GatewayServiceApplication.class);

		app.addInitializers(ctx -> {
			// 1. Configure Dotenv safely to prevent classpath file execution panic loops inside containers
			Dotenv dotenv = Dotenv.configure()
					.ignoreIfMalformed()
					.ignoreIfMissing()
					.load();

			// 2. Resolution strategy tree: Look at the Linux System Environment context first, fallback to the file asset second
			String jwtSecret = System.getenv("SECRET") != null ? System.getenv("SECRET") : dotenv.get("SECRET");

			Map<String, Object> properties = new HashMap<>();

			if (jwtSecret != null) {
				properties.put("SECRET", jwtSecret);
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
