package com.eswar.gatewayservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

@Configuration
public class CorsGlobalConfig {


    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedOrigin("http://10.105.105.219:7777");
        config.addAllowedOrigin("http://10.105.105.219:8080");
        config.addAllowedOrigin("http://localhost:4200");
        config.addAllowedOrigin("http://10.137.236.219:7777");
        config.addAllowedOrigin("http://10.137.236.219:8080");
        config.addAllowedOrigin("https://2968-2409-40f0-9-315-94d8-3c53-c0a0-a3dc.ngrok-free.app");
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsWebFilter(source);
    }
}
