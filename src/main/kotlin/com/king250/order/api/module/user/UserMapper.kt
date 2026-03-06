package com.king250.order.api.module.user

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.MappingConstants
import org.mapstruct.Mappings

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
interface UserMapper {
    fun toResponseList(users: List<User>): List<UserResponse>

    fun toResponse(user: User): UserResponse

    @Mappings(
        Mapping(target = "id", ignore = true),
        Mapping(target = "creditScore", ignore = true),
        Mapping(target = "createdAt", ignore = true),
        Mapping(target = "updatedAt", ignore = true),
        Mapping(target = "deletedAt", ignore = true)
    )
    fun toEntity(request: UserCreateRequest): User
}
