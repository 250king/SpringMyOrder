package com.king250.order.api.integration.jd

import jakarta.validation.constraints.NotBlank
import org.hibernate.validator.constraints.URL
import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.jd")
data class JdProperties(
    @field:NotBlank
    val key: String,

    @field:NotBlank
    val secret: String,

    @field:NotBlank
    val customerId: String,

    @field:NotBlank
    val shopId: String,

    @field:NotBlank
    @field:URL
    val webhook: String
)
