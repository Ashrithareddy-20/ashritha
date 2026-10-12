package com.planora.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    public static final String SECURITY_SCHEME_NAME = "Bearer Authentication";

    @Bean
    public OpenAPI planoraOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Planora REST API")
                        .description("API documentation for Planora - AI-Powered Event Planning and Vendor Recommendation System. " +
                                "To access protected endpoints, obtain a JWT via /api/auth/login or /api/auth/signup, " +
                                "then click the 'Authorize' button and enter your Bearer token.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Planora Engineering Team")
                                .email("support@planora.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME,
                                new SecurityScheme()
                                        .name(SECURITY_SCHEME_NAME)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Enter your JWT Bearer token to authorize requests. Format: Bearer <token>")
                        )
                );
    }
}
