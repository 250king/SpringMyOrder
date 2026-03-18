package com.king250.order.api.integration.jd

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy::class)
data class JdResponse<T>(
    val data: T? = null,

    val error: JdError? = null,

    val result: String
)
