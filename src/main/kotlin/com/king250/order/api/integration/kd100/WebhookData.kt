package com.king250.order.api.integration.kd100

data class WebhookData(
    val kuaidinum: String,

    val status: Status,

    val message: String
)
