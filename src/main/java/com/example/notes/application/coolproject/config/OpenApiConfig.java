package com.example.notes.application.coolproject.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

  private static final String BEARER_SCHEME_NAME = "bearerAuth";

  @Bean
  public OpenAPI customOpenAPI() {
    return new OpenAPI()
        .info(
            new Info()
                .title("Expense Tracker Api")
                .description("API for managing personal expenses")
                .version("1.0.0")
                .contact(
                    new Contact().name("Rustam Ismayilov").email("rustamismayilov898@gmail.com")))
        .addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME_NAME))
        .components(
            new Components()
                .addSecuritySchemes(
                    BEARER_SCHEME_NAME,
                    new SecurityScheme()
                        .name(BEARER_SCHEME_NAME)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")));
  }
}
