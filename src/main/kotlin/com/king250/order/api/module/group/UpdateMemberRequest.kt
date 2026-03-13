package com.king250.order.api.module.group

import com.king250.order.jooq.enums.GroupRole

data class UpdateMemberRequest(
    val role: GroupRole,
)
