package com.king250.order.api.config

import com.king250.order.api.common.JsonNullableMapper
import org.mapstruct.MapperConfig as MapStructMapperConfig
import org.mapstruct.MappingConstants
import org.mapstruct.NullValuePropertyMappingStrategy

@MapStructMapperConfig(
    componentModel = MappingConstants.ComponentModel.SPRING,
    uses = [JsonNullableMapper::class],
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
interface MapperConfig
