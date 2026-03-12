package com.king250.order.api.module.group

import com.king250.order.api.module.user.UserResponse
import com.king250.order.jooq.enums.Role
import java.time.Instant

data class MemberResponse(
    val user: UserResponse,

    val role: Role,

    val createdAt: Instant,
)
