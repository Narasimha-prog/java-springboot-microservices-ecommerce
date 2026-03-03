package com.eswar.gatewayservice.runner;

import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class BinderDebugger {
    public BinderDebugger(Environment env) {
        Binder binder = Binder.get(env);
        List<RouteDefinition> routes = binder.bind("spring.cloud.gateway.routes", Bindable.listOf(RouteDefinition.class))
                .orElse(Collections.emptyList());
        System.out.println("=== Binder read these routes ===");
        routes.forEach(System.out::println);
    }
}
