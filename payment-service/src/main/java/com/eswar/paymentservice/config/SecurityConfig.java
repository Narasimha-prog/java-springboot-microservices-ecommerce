package com.eswar.paymentservice.config;
import org.jspecify.annotations.NonNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private static final String[] SWAGGER_WHITELIST = {
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/v3/api-docs.yaml",
            "/v3/api-docs"
    };
    private static final String[] ACTUATOR_WHITELIST = {
            "/actuator/health",
            "/actuator/info"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(@NonNull HttpSecurity httpSecurity){



        return  httpSecurity
                .cors(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(
                        request -> request


                                .requestMatchers(
                                        SWAGGER_WHITELIST
                                ).permitAll()
                                .requestMatchers(
                                        ACTUATOR_WHITELIST
                                ).permitAll()
                                .requestMatchers("/docs/**", "/css/**", "/js/**").permitAll()
                                // Public product view
                                .requestMatchers(HttpMethod.GET, "/api/v1/payments").hasRole("ADMIN")
                                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                                // Admin only



                                .anyRequest().authenticated()


                ).formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .addFilterBefore(new com.eswar.paymentservice.filter.HeaderAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
                .build();


    }
}
