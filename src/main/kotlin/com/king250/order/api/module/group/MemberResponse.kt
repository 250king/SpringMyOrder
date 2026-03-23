package com.king250.order.api.module.group

import com.king250.order.api.module.user.UserResponse
import java.time.Instant

data class MemberResponse(
    val user: UserResponse,

    val createdAt: Instant,
)
