package com.spring.security.web.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        SecurityScheme bearerScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("JWT Bearer Token");

        SecurityRequirement bearerRequirement = new SecurityRequirement().addList("Bearer Authentication");

        return new OpenAPI()
                .info(new Info()
                        .title("Spring Security API")
                        .version("1.0.0")
                        .description("Spring Security API Document")
                        .license(new License().name("License").url("https://www.sanwenyukaochi.license.com"))
                        .contact(new Contact()
                                .name("YiFan Song")
                                .email("sanwenyukaochi@email.com")
                                .url("https://www.sanwenyukaochi.blog.com")))
                .externalDocs(new ExternalDocumentation()
                        .description("Project Document")
                        .url("https://www.sanwenyukaochi.blog.com"))
                .components(new Components().addSecuritySchemes("Bearer Authentication", bearerScheme))
                .addSecurityItem(bearerRequirement);
    }
}
