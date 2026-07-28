package com.mtbs.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI/Swagger metadata plus a JWT bearer security scheme, so the Swagger UI exposes an
 * "Authorize" button for pasting a token from {@code POST /auth/login}.
 */
@Configuration
public class OpenApiConfig {

  private static final String BEARER = "bearer-jwt";

  @Bean
  public OpenAPI mtbsOpenApi() {
    return new OpenAPI()
        .info(new Info()
            .title("Movie Ticket Booking System API")
            .version("v1")
            .description("Seat-level booking with time-bound holds, tiered pricing, discounts, "
                + "mock payments, and configurable refunds. Authenticate via POST /auth/login, "
                + "then Authorize with the returned token."))
        .addSecurityItem(new SecurityRequirement().addList(BEARER))
        .components(new Components().addSecuritySchemes(BEARER,
            new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")));
  }
}
