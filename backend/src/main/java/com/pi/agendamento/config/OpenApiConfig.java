package com.pi.agendamento.config;

import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;

// documentacao openapi com jwt por padrao; rotas publicas anulam com @SecurityRequirements
@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Agendamento de Consultas",
                version = "v1",
                description = "Token: POST /api/auth/login e colar o `token` em Authorize."),
        security = @SecurityRequirement(name = OpenApiConfig.BEARER))
@SecurityScheme(
        name = OpenApiConfig.BEARER,
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT")
public class OpenApiConfig {

    static final String BEARER = "bearerAuth";
}
