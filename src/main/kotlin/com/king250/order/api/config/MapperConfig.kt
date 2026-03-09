package com.king250.order.api.config

import com.king250.order.api.common.NullableMapper
import org.mapstruct.MapperConfig as MapStructMapperConfig
import org.mapstruct.MappingConstants
import org.mapstruct.NullValuePropertyMappingStrategy
import org.mapstruct.ReportingPolicy

@MapStructMapperConfig(
    componentModel = MappingConstants.ComponentModel.SPRING,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    uses = [NullableMapper::class],
)
interface MapperConfig
