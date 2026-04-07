package com.eswar.userservice.config;

import com.eswar.userservice.filter.HeaderAuthenticationFilter;
import com.eswar.userservice.handler.SecurityExceptionHandler;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {


    private final SecurityExceptionHandler securityExceptionHandler;

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
    private static final String[] STATIC_DOC={
            "/docs/**", "/css/**", "/js/**"
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
                                .requestMatchers(
                                        STATIC_DOC
                                ).permitAll()
                                // Public product view
                                .requestMatchers(HttpMethod.POST, "/api/v1/users/**").permitAll()
                                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                                .anyRequest().authenticated()
                        

                )
                .sessionManagement(
                        session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                ).formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .addFilterBefore(new HeaderAuthenticationFilter(),
                        UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(ex->
                ex.authenticationEntryPoint(securityExceptionHandler)
                .accessDeniedHandler(securityExceptionHandler))
                .build();


    }
}
