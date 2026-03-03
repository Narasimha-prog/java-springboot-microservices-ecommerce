package com.eswar.gatewayservice.runner;

import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.Collections;
import java.util.List;

@Component
public class CustomRouteLocator implements RouteDefinitionLocator {

    private final Environment environment;

    public CustomRouteLocator(Environment environment) {
        this.environment = environment;
    }

    @Override
    public Flux<RouteDefinition> getRouteDefinitions() {
        // Use Binder to read spring.cloud.gateway.routes
        Binder binder = Binder.get(environment);
        List<RouteDefinition> routes = binder.bind("spring.cloud.gateway.routes", Bindable.listOf(RouteDefinition.class))
                .orElse(Collections.emptyList());
        // Log or modify routes
        routes.forEach(route -> System.out.println("Loaded route: " + route));

        return Flux.fromIterable(routes);
    }
}
