package com.srinu.chatbot_service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class ChromaConfig {

    @Value("${chromadb.url:http://localhost:8000}")
    private String chromaUrl;

    @Bean
    public RestClient chromaRestClient() {
        return RestClient.builder()
                .baseUrl(chromaUrl)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }
}
