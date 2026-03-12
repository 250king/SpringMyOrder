package com.king250.order.api.module.group

import com.king250.order.jooq.enums.GroupStatus
import java.time.Instant

data class GroupResponse(
    val id: Long,

    val name: String,

    val qq: String,

    val status: GroupStatus,

    val deadline: Instant,
)