package com.king250.order.api.integration.napcat

import jakarta.validation.constraints.NotBlank
import org.hibernate.validator.constraints.URL
import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.napcat")
data class NapcatProperties(
    @field:NotBlank
    @field:URL
    val url: String,

    @field:NotBlank
    val token: String,
)
