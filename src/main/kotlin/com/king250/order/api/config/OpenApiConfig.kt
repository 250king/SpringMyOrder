package com.king250.order.api.config

import com.fasterxml.jackson.databind.type.TypeFactory
import io.swagger.v3.core.converter.AnnotatedType
import io.swagger.v3.core.converter.ModelConverters
import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.media.Schema
import io.swagger.v3.oas.models.security.SecurityScheme
import org.openapitools.jackson.nullable.JsonNullable
import org.springdoc.core.customizers.OpenApiCustomizer
import org.springdoc.core.customizers.PropertyCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig {
    @Bean
    fun defaultOpenAPI(): OpenAPI {
        return OpenAPI()
            .components(Components()
                .addSecuritySchemes("BearerAuth",
                    SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                )
            )
    }

    @Bean
    fun jsonNullablePropertyCustomizer(): PropertyCustomizer = PropertyCustomizer { schema, type ->
        val rawType = type.type ?: return@PropertyCustomizer schema
        val javaType = TypeFactory.defaultInstance().constructType(rawType)
        if (javaType.rawClass != JsonNullable::class.java || javaType.containedTypeCount() == 0) {
            return@PropertyCustomizer schema
        }
        val wrappedType = javaType.containedType(0)
        val resolved = ModelConverters.getInstance()
            .resolveAsResolvedSchema(AnnotatedType(wrappedType).resolveAsRef(false))
            .schema
        val unwrapped = (resolved ?: schema) as Schema<Any>
        unwrapped.nullable = true
        unwrapped.addExtension("x-json-nullable", true)
        unwrapped
    }

    @Bean
    fun jsonNullableOptionalCustomizer(): OpenApiCustomizer = OpenApiCustomizer { openApi ->
        openApi.components?.schemas?.values?.forEach { modelSchema ->
            val properties = modelSchema.properties ?: return@forEach
            val removeRequired = mutableSetOf<String>()
            properties.forEach { (name, propertySchemaAny) ->
                val propertySchema = propertySchemaAny ?: return@forEach
                val isJsonNullable = propertySchema.extensions?.remove("x-json-nullable") == true
                if (isJsonNullable) {
                    propertySchema.nullable = true
                    removeRequired += name
                }
            }
            if (removeRequired.isNotEmpty()) {
                modelSchema.required = modelSchema.required
                    ?.filterNot { it in removeRequired }
                    ?.ifEmpty { null }
            }
        }
        openApi.components?.schemas?.keys
            ?.filter { it.startsWith("JsonNullable") }
            ?.toList()
            ?.forEach { openApi.components.schemas.remove(it) }
    }
}
