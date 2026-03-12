package com.king250.order.api.common

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.domain.Sort.Direction
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

open class QueryRequest(
    var id: Long? = null,

    var keyword: String? = null,

    @field:Min(value = 1)
    var page: Int = 1,

    @field:Min(value = 1)
    @field:Max(value = 50)
    var size: Int = 10,

    var sort: String = "id",

    var order: String = "asc"
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
