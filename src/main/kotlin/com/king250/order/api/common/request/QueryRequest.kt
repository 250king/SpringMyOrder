package com.king250.order.api.common.request

import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.domain.Sort.Direction

open class QueryRequest(
    open var id: Long? = null,
    open var keyword: String? = null,
    open var page: Int = 0,
    open var size: Int = 10,
    open var sort: String = "id",
    open var order: Direction = Direction.DESC
) {
    fun toPageable() = PageRequest.of(
        page,
        size,
        Sort.by(order, sort)
    )
}
