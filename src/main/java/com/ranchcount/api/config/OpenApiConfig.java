package com.ranchcount.api.config;

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

    public static final String BEARER_SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI ranchCountOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("RanchCount (RanchZ) RESTful API")
                        .version("1.0.0")
                        .description("API RESTful para la gestion operativa, ganadera, agricola y financiera "
                                + "de un rancho, consumida por la aplicacion movil nativa Android (Kotlin + Retrofit).")
                        .contact(new Contact()
                                .name("Equipo de Arquitectura RanchCount")
                                .email("soporte@ranchcount.local")))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(BEARER_SCHEME_NAME, new SecurityScheme()
                                .name(BEARER_SCHEME_NAME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Ingrese el token JWT obtenido en /api/v1/auth/login")));
    }
}
