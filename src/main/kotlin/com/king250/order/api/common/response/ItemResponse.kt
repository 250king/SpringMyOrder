package com.king250.order.api.common.response

data class ItemResponse<T>(
    val items: List<T>,

    val total: Long
)
