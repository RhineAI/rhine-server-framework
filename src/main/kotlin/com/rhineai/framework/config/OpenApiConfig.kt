package com.rhineai.framework.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.License
import io.swagger.v3.oas.models.security.SecurityScheme
import io.swagger.v3.oas.models.security.SecurityRequirement
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@ConditionalOnClass(OpenAPI::class)
class OpenApiConfig {

    @Bean
    @ConditionalOnMissingBean(OpenAPI::class)
    @ConditionalOnProperty(prefix = "springdoc", name = ["api-docs.enabled"], havingValue = "true", matchIfMissing = true)
    fun openAPI(): OpenAPI {
        val bearerScheme = SecurityScheme()
            .type(SecurityScheme.Type.HTTP)
            .scheme("bearer")
            .bearerFormat("JWT")
            .name("Authorization")
            .description("Input your JWT token. Example: Bearer eyJhbGciOi...")

        return OpenAPI()
            .info(
                Info()
                    .title("O-Flow APIs")
                    .description("O-Flow unified API specification")
                    .version("v1")
                    .license(License().name("Proprietary"))
            )
            .components(
                io.swagger.v3.oas.models.Components()
                    .addSecuritySchemes("bearerAuth", bearerScheme)
            )
            .addSecurityItem(SecurityRequirement().addList("bearerAuth"))
    }
}