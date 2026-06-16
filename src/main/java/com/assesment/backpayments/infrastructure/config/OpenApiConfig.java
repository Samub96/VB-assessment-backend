package com.assesment.backpayments.infrastructure.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;

/**
 * Configuración base de OpenAPI para documentar el API y soportar JWT.
 */
@OpenAPIDefinition(
        info = @Info(
                title = "BackPayments API",
                version = "v1",
                description = "API para gestión de órdenes de pago"
        )
)
@SecurityScheme(
        name = "BearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT"
)
public class OpenApiConfig {
}
