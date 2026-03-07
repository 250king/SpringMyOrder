package com.king250.order.api.group

import java.io.Serializable

data class GroupUserId(
    var user: Long,
    var group: Long
) : Serializable
