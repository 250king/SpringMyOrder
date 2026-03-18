package com.king250.order.api.integration.jd

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy::class)
data class CreateUrlRequest(
    val requestNum: String,

    val amount: String,

    var customerNum: String? = null,

    var shopNum: String? = null,

    var callbackUrl: String? = null,

    val source: String = "API"
)
