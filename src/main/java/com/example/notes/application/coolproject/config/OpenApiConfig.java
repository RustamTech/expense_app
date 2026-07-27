package com.example.notes.application.coolproject.config;

import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI(){
        return new OpenAPI()
                .info(new Info()
                        .title("Expense Tracker Api")
                        .description("API for managing personal expenses")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Rustam Ismayilov")
                                .email("rustamismayilov898@gmail.com")));
    }
}
