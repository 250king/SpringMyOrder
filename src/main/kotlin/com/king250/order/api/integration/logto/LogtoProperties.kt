package com.king250.order.api.integration.logto

import jakarta.validation.constraints.NotBlank
import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.logto")
data class LogtoProperties(
    @field:NotBlank
    val clientId: String,

    @field:NotBlank
    val clientSecret: String,

    @field:NotBlank
    val key: String,
)
