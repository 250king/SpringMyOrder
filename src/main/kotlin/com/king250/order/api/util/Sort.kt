package com.king250.order.api.util

import org.jooq.SortField
import org.jooq.Table
import org.springframework.data.domain.Sort

fun Sort.toJooq(table: Table<*>): List<SortField<*>> {
    return this.map { order ->
        val field = table.field(order.property) ?: throw IllegalArgumentException("Unknown property: ${order.property}")
        if (order.isDescending) {
            field.desc()
        } else {
            field.asc()
        }
    }.toList()
}
