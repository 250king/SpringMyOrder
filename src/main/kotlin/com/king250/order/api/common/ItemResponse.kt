package com.king250.order.api.common

import org.springframework.data.domain.Page

data class ItemResponse<T>(
    val items: List<T>,

    val total: Long
) {
    companion object {
        fun <S, T> fromPage(page: Page<S>, mapper: (S) -> T): ItemResponse<T> {
            return ItemResponse(
                items = page.content.map(mapper),
                total = page.totalElements
            )
        }
    }
}
