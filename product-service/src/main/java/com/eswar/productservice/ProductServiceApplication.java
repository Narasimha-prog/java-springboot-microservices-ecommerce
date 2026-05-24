package com.eswar.productservice;

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
import org.springframework.core.env.MapPropertySource;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@SpringBootApplication
@EnableJpaAuditing
@OpenAPIDefinition(
		info = @Info(title = "Product Service API", version = "1.0.0"),
		security = @SecurityRequirement(name = "JWT"),
		servers = {
				@Server(
						description = "API Gateway",
						url = "http://localhost:8080"
				),
				@Server(
						description = "API Product Service",
						url = "http://localhost:8083"
				)
		}
)
@SecurityScheme(
		name = "JWT",                   // Name referenced in @SecurityRequirement
		type = SecuritySchemeType.HTTP, // HTTP type
		scheme = "bearer",              // Bearer authentication
		bearerFormat = "JWT",           // Optional, shows "JWT" in Swagger UI
		in = SecuritySchemeIn.HEADER    // Token passed in header
)
@EnableScheduling
public class ProductServiceApplication {

	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(ProductServiceApplication.class);

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
