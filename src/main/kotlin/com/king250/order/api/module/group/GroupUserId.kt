package com.king250.order.api.module.group

import java.io.Serializable

data class GroupUserId(
    var user: Long,
    var group: Long
) : Serializable
