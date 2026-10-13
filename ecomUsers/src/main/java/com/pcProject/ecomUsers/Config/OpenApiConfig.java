package com.pcProject.ecomUsers.Config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI myAPI() {
        return new OpenAPI().info(new Info()
                .title("EcomUsers Backend API")
                .version("1.0")
                .description("Backend API documentation"));
    }
}
