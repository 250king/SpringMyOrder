package com.king250.order.api.integration.kd100

data class CreateOrderRequest(
    val kuaidicom: String,

    val recManName: String,

    val recManMobile: String,

    val recManPrintAddr: String,

    val sendManName: String,

    val sendManMobile: String,

    val sendManPrintAddr: String,

    val cargo: String,
)
