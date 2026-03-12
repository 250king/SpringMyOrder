package com.king250.order.api.util

import com.king250.order.api.common.ItemResponse
import org.springframework.data.domain.Page

fun <T> Page<T>.toItem(): ItemResponse<T> {
    return ItemResponse(
        items = this.content,
        total = this.totalElements
    )
}

inline fun <S, T> Page<S>.toItem(mapper: (S) -> T): ItemResponse<T> {
    return ItemResponse(
        items = this.content.map(mapper),
        total = this.totalElements
    )
}
