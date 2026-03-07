package com.king250.order.api.common.annotation

import com.king250.order.api.common.mapper.JsonNullableMapper
import org.mapstruct.Mapper as MapStructMapper
import org.mapstruct.MappingConstants
import org.mapstruct.NullValuePropertyMappingStrategy

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
@MapStructMapper(
    componentModel = MappingConstants.ComponentModel.SPRING,
    uses = [JsonNullableMapper::class],
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
annotation class Mapper
