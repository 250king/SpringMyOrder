package com.king250.order.api.module.group

import com.king250.order.api.config.MapperConfig
import com.king250.order.jooq.tables.records.GroupRecord
import org.mapstruct.Mapper
import org.mapstruct.MappingTarget

@Mapper(config = MapperConfig::class)
interface GroupMapper {
    fun toResponse(group: GroupRecord): GroupResponse

    fun toEntity(request: GroupCreateRequest): GroupRecord

    fun updateEntity(request: GroupUpdateRequest, @MappingTarget group: GroupRecord): GroupRecord
}
