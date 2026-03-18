package com.king250.order.api.integration.kd100

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy::class)
data class OrderResponse(
    val taskId: String,

    val orderId: String,

    val kuaidinum: String?,

    val pollToken: String,
)
