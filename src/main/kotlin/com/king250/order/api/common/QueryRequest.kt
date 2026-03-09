package com.king250.order.api.common

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.domain.Sort.Direction
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

open class QueryRequest(
    open var id: Long? = null,

    open var keyword: String? = null,

    @field:Min(value = 1)
    open var page: Int = 1,

    @field:Min(value = 1)
    @field:Max(value = 50)
    open var size: Int = 10,

    open var sort: String = "id",

    open var order: String = "asc"
) {
    fun toPageable(): PageRequest {
        try {
            val direction = Direction.fromString(order.uppercase())
            return PageRequest.of(
                (page - 1).coerceAtLeast(0),
                size,
                Sort.by(direction, sort)
            )
        } catch (e: IllegalArgumentException) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, e.message)
        }
    }
}
