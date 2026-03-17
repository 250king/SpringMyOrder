package com.king250.order.api.integration.jd

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.jd")
data class JdProperties(
    val key: String,

    val secret: String,

    val customerId: String,

    val shopId: String,
)
