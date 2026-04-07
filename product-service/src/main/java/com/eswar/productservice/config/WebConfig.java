package com.eswar.productservice.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        // 1. Get the current directory (which is /.../product-service)
        String userDir = System.getProperty("user.dir");
        Path currentPath = Paths.get(userDir);

        // 2. We need to reach the 'uploads' folder in the root
        // If you run from inside product-service, it's ../uploads
        Path uploadsPath = currentPath.resolve("uploads").normalize();

        String location = "file:" + uploadsPath.toString() + "/";

        // Check your console after restart - it MUST match your real path
        System.out.println("CORRECTED Serving images from: " + location);

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(location)
                .setCachePeriod(0);
    }
}