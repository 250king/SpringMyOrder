package com.king250.order.api.integration.logto

data class WebhookData(
    val event: String,

    val data: UserData?,

    val params: ParamsData,
)
