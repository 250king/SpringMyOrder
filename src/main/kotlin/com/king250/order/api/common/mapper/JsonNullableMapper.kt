package com.king250.order.api.common.mapper

import org.mapstruct.Condition
import org.mapstruct.Mapper
import org.mapstruct.MappingConstants
import org.openapitools.jackson.nullable.JsonNullable

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
interface JsonNullableMapper {
    fun <T> unwrap(nullable: JsonNullable<T>): T? = nullable.get()

    @Condition
    fun isPresent(nullable: JsonNullable<*>?): Boolean {
        return nullable != null && nullable.isPresent
    }
}