package com.king250.order.api.integration.kd100

data class WebhookRequest(
    val kuaidinum: String,

    val status: Status,

    val message: String
)
