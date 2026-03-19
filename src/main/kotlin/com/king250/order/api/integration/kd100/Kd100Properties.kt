package com.king250.order.api.integration.kd100

import jakarta.validation.constraints.NotBlank
import org.hibernate.validator.constraints.URL
import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.kd100")
data class Kd100Properties(
    @field:NotBlank
    val key: String,

    @field:NotBlank
    val secret: String,

    @field:NotBlank
    val salt: String,

    @field:NotBlank
    @field:URL
    val webhook: String,
)
