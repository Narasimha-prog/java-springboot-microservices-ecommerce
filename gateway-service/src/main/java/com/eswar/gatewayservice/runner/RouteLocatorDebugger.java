package com.eswar.gatewayservice.runner;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.stereotype.Component;

@Component
public class RouteLocatorDebugger {

    public RouteLocatorDebugger(RouteLocator locator) {
        locator.getRoutes()
                .subscribe(route -> System.out.println("=== Active Gateway Route: " + route.getId()));
    }
}
