package com.taskmanagement.taskmanagement.config;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI customOpenAPI(){
        return new OpenAPI() 
        .info(new Info().title("Task Management API")
        .version("1.0.0")
        .description(
                    "A complete Task Management REST API built with " +
                    "Spring Boot. Features include JWT authentication, " +
                    "role-based access control, task CRUD operations, " +
                    "pagination, filtering, search and statistics.")
        .contact(new Contact()
                    .name("Sumit Uppal")
                    .email("sumituppal03@gmail.com")
                    .url("https://github.com/sumituppal03"))
        .license(new License()
                    .name("MIT License")
                    .url("https://opensource.org/licenses/MIT"))
        
        )
        .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
        .components(new Components()
                            .addSecuritySchemes("Bearer Authentication",new SecurityScheme().type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")
                    .name("Bearer Authentication")
                    .description(
                            "Enter JWT token. " +
                            "Get token from /api/auth/login endpoint. " +
                            "Format: your-jwt-token-here (WITHOUT Bearer prefix)")
                ));


    }
}
