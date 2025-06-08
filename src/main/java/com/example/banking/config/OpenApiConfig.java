package com.example.banking.config;

import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI bankingOpenAPI() {
        return new OpenAPI()
                .info(new Info().title("Banking API")
                .description("Simple banking service")
                .version("1.0"));
    }
}
