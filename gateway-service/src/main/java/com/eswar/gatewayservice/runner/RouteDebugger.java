package com.eswar.gatewayservice.runner;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;
import org.springframework.stereotype.Component;

@Component
public class RouteDebugger implements CommandLineRunner {

    private final RouteDefinitionLocator locator;

    public RouteDebugger(RouteDefinitionLocator locator) {
        this.locator = locator;
    }

    @Override
    public void run(String... args) throws Exception {
        locator.getRouteDefinitions()
                .collectList()
                .subscribe(routes -> {
                    System.out.println("=== Routes from Config Server ===");
                    routes.forEach(System.out::println);
                });
    }


}
