package com.eswar.gatewayservice.filter;

import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.stereotype.Component;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
@Slf4j
@Component
public class LoggingGlobalFilter implements GlobalFilter, Ordered {

    public LoggingGlobalFilter() {
        log.info("LoggingGlobalFilter initialized");
    }

    @Override
    public @NonNull Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        log.info("Incoming request: {} {}", exchange.getRequest().getMethod(), exchange.getRequest().getPath());
        return chain.filter(exchange)
                .doFinally(signal -> log.info("Request completed, signal={}", signal));
    }

    @Override
    public int getOrder() {
        return -1;
    }
}