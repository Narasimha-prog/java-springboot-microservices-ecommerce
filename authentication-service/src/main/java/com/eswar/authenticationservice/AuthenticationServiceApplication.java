package com.eswar.authenticationservice;

import io.github.cdimascio.dotenv.Dotenv;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.MapPropertySource;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@SpringBootApplication
@EnableDiscoveryClient
@OpenAPIDefinition(
		info = @Info(title = "Authentication Service API", version = "1.0.0"),
		security = @SecurityRequirement(name = "JWT"),
		servers = {
				@Server(
						description = "API Gateway",
						url = "http://localhost:8080"
				),
				@Server(
						description = "API Authentication",
						url = "http://localhost:8082"
				)
		}
)
@SecurityScheme(
		name = "JWT",                   // Name referenced in @SecurityRequirement
		type = SecuritySchemeType.HTTP, // HTTP type
		scheme = "bearer",              // Bearer authentication
		bearerFormat = "JWT",           // Optional, shows "JWT" in Swagger UI
		in = SecuritySchemeIn.HEADER   // Token passed in header


)
public class AuthenticationServiceApplication {

	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(AuthenticationServiceApplication.class);

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
