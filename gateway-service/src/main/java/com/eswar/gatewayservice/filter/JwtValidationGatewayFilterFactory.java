package com.eswar.gatewayservice.filter;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.cloud.gateway.filter.factory.GatewayFilterFactory;
import org.springframework.stereotype.Component;

@Component("JwtValidation")
public class JwtValidationGatewayFilterFactory
        extends AbstractGatewayFilterFactory<JwtValidationGatewayFilterFactory.Config> {

    public JwtValidationGatewayFilterFactory() {
        super(Config.class); // <-- tells Spring what your config class is
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            // TODO: Add JWT validation logic here
            return chain.filter(exchange);
        };
    }

    public static class Config {
        // Add fields if needed (header names, secret keys, etc.)
    }
}