package com.king250.order.api.integration.jd

data class CreateUrlRequest(
    val requestNum: String,

    val amount: String,

    var customerNum: String? = null,

    var shopNum: String? = null,

    var callbackUrl: String? = null,

    val source: String = "API"
)
