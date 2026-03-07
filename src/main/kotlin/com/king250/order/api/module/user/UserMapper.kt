package com.king250.order.api.module.user

import com.king250.order.api.common.annotation.Mapper
import com.king250.order.jooq.tables.records.UserRecord
import org.mapstruct.MappingTarget

@Mapper
interface UserMapper {
    fun toResponseList(users: List<UserRecord>): List<UserResponse>

    fun toResponse(user: UserRecord): UserResponse

    fun toEntity(request: UserCreateRequest): UserRecord

    fun updateEntity(request: UserUpdateRequest, @MappingTarget user: UserRecord): UserRecord
}
