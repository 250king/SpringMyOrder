package com.king250.order.api.module.background

import jakarta.validation.constraints.NotBlank
import org.hibernate.validator.constraints.URL
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.validation.annotation.Validated

@Validated
@ConfigurationProperties(prefix = "app.background")
data class BackgroundProperties(
    @field:URL
    val url: String,

    @field:URL
    val default: String,

    @field:NotBlank
    val path: String = "background"
)
