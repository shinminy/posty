package com.posty.postingapi.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    public static final String API_KEY_SCHEME_NAME = "apiKey";

    private final ApiConfig apiConfig;

    public OpenApiConfig(ApiConfig apiConfig) {
        this.apiConfig = apiConfig;
    }

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .components(new Components()
                        .addSecuritySchemes(API_KEY_SCHEME_NAME, new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)
                                .name(apiConfig.getKeyHeaderName())))
                .addSecurityItem(new SecurityRequirement().addList(API_KEY_SCHEME_NAME));
    }
}
