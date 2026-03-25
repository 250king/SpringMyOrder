package com.king250.order.api.module.user

import com.king250.order.api.config.MapperConfig
import com.king250.order.jooq.tables.records.UserRecord
import org.mapstruct.Mapper

@Mapper(config = MapperConfig::class)
interface UserMapper {
    fun toResponse(user: UserRecord): UserResponse

    fun toMeResponse(user: UserRecord, isAdmin: Boolean): MeResponse
}
