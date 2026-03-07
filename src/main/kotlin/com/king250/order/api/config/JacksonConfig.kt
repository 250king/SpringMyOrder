package com.king250.order.api.config

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.openapitools.jackson.nullable.JsonNullableModule
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder


@Configuration
class JacksonConfig {
    @Bean
    fun jsonCustomizer(): Jackson2ObjectMapperBuilderCustomizer {
        return Jackson2ObjectMapperBuilderCustomizer { builder: Jackson2ObjectMapperBuilder? ->
            builder!!.modules(
                JsonNullableModule(),
                KotlinModule.Builder().build()
            )
            builder.featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        }
    }
}