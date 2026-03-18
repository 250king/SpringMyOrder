package com.king250.order.api.integration.kd100

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy::class)
data class Kd100Response<T>(
    val result: Boolean,

    val returnCode: String,

    val message: String,

    val data: T
)