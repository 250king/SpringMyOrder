package com.king250.order.api.module.group

import com.king250.order.api.module.user.UserResponse
import com.king250.order.jooq.enums.GroupRole
import java.time.Instant

data class MemberResponse(
    val user: UserResponse,

    val role: GroupRole,

    val createdAt: Instant,
)
