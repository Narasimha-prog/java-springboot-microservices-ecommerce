package com.eswar.authenticationservice.security;
import  com.eswar.authenticationservice.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@EnableWebSecurity
@Configuration
@RequiredArgsConstructor
public class AuthenticationSecurity {


    private final JwtAuthenticationFilter jwtAuthenticationFilter;

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
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity){



     return  httpSecurity
             .csrf(AbstractHttpConfigurer::disable)
             .authorizeHttpRequests(
                           request -> request


                                   .requestMatchers(
                                           SWAGGER_WHITELIST
                                   ).permitAll()
                                   .requestMatchers(
                                           ACTUATOR_WHITELIST
                                   ).permitAll()
                                   .requestMatchers(
                                          "/api/v1/auth/login",
                                            "/api/v1/auth/refresh"
                                   ).permitAll()
                                   .anyRequest().authenticated()

                   )
             .addFilterBefore(jwtAuthenticationFilter,
                              UsernamePasswordAuthenticationFilter.class)
             .build();


    }
}
