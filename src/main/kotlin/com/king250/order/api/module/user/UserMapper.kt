package com.king250.order.api.module.user

import com.king250.order.api.config.MapperConfig
import com.king250.order.jooq.tables.records.UserRecord
import org.mapstruct.Mapper
import org.mapstruct.MappingTarget

@Mapper(config = MapperConfig::class)
interface UserMapper {
    fun toResponse(user: UserRecord): UserResponse

    fun toEntity(request: CreateUserRequest): UserRecord

    fun updateEntity(request: UpdateUserRequest, @MappingTarget user: UserRecord): UserRecord
}
