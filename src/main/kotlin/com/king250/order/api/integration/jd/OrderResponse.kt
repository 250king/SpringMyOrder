package com.king250.order.api.integration.jd

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import java.time.Instant

@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy::class)
data class OrderResponse(
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    val completeTime: Instant,

    val payWayEnum: Method,
)
