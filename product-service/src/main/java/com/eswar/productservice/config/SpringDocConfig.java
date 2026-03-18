package com.eswar.productservice.config;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringDocConfig {
    @Bean
    public OpenApiCustomizer fixPageableSort() {
        return openApi -> openApi.getPaths().values().forEach(path ->
                                                                      path.readOperations().forEach(operation -> {
                                                                          if (operation.getParameters() != null) {
                                                                              operation.getParameters().stream()
                                                                                      .filter(p -> "sort".equals(p.getName()))
                                                                                      .forEach(p -> {
                                                                                          p.setExample("name,asc"); // Render as string
                                                                                          p.setDescription("Sort format: property,(asc|desc). E.g., sort=name,asc");
                                                                                      });
                                                                          }
                                                                      })
        );
    }

}
