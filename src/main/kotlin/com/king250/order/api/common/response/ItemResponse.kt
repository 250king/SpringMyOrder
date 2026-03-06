package com.king250.order.api.common.response

import com.fasterxml.jackson.annotation.JsonProperty

data class ItemResponse<T>(
    @JsonProperty("items")
    val items: List<T>,

    @JsonProperty("total")
    val total: Long
)
