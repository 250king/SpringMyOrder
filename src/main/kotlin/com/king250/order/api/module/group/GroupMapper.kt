package com.king250.order.api.module.group

import com.king250.order.api.config.MapperConfig
import com.king250.order.api.module.user.UserResponse
import com.king250.order.jooq.tables.records.GroupRecord
import com.king250.order.jooq.tables.records.GroupUserRecord
import com.king250.order.jooq.tables.records.UserRecord
import com.king250.order.jooq.tables.references.GROUP_USER
import com.king250.order.jooq.tables.references.USER
import org.jooq.Record
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.MappingTarget

@Mapper(config = MapperConfig::class)
interface GroupMapper {
    fun toResponse(group: GroupRecord): GroupResponse

    fun toMemberResponse(record: Record): MemberResponse {
        val groupUser = record.into(GROUP_USER)
        val user = record.into(USER)
        return mergeToResponse(groupUser, user)
    }

    fun toEntity(request: CreateGroupRequest): GroupRecord

    fun updateEntity(request: UpdateGroupRequest, @MappingTarget group: GroupRecord): GroupRecord

    @Mapping(target = "user", source = "userRecord")
    @Mapping(target = "createdAt", source = "groupUserRecord.createdAt")
    fun mergeToResponse(groupUserRecord: GroupUserRecord, userRecord: UserRecord): MemberResponse

    fun mapUser(record: UserRecord): UserResponse
}
