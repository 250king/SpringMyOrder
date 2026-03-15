package com.king250.order.api.integration.kd100

data class OrderResponse(
    val taskId: String,

    val orderId: String,

    val kuaidinum: String?,

    val pollToken: String,
)
