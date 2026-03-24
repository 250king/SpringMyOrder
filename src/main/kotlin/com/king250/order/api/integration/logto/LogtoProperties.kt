package com.king250.order.api.integration.logto

import jakarta.validation.constraints.NotBlank
import org.hibernate.validator.constraints.URL
import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.logto")
data class LogtoProperties(
    @field:NotBlank
    @field:URL
    val url: String,

    @field:NotBlank
    val clientId: String,

    @field:NotBlank
    val clientSecret: String,
)
