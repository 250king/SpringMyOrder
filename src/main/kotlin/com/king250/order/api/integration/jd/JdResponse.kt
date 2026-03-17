package com.king250.order.api.integration.jd

data class JdResponse<T>(
    val data: T,

    val result: String
)
