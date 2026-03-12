package com.king250.order.api.util

import org.jooq.Field
import org.jooq.SortField
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

fun Sort.toJooq(mappings: Map<String, Field<*>>): List<SortField<*>> {
    return this.map { order ->
        val field = mappings[order.property]
            ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid sort property: ${order.property}")
        if (order.isDescending) field.desc() else field.asc()
    }.toList()
}
