package com.king250.order.api.integration.kd100

data class Kd100Response<T>(
    val result: Boolean,

    val returnCode: String,

    val message: String,

    val data: T
)