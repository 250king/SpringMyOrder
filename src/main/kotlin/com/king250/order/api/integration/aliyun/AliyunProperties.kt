package com.king250.order.api.integration.aliyun

import jakarta.validation.constraints.NotBlank
import org.hibernate.validator.constraints.URL
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.validation.annotation.Validated

@Validated
@ConfigurationProperties(prefix = "app.aliyun")
data class AliyunProperties(
    @field:URL
    val endpoint: String,

    @field:NotBlank
    val key: String,

    @field:NotBlank
    val secret: String,

    @field:NotBlank
    val bucket: String,
)
