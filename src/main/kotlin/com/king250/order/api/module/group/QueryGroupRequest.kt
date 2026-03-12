package com.king250.order.api.module.group

import com.king250.order.api.common.QueryRequest
import com.king250.order.jooq.enums.GroupStatus

class QueryGroupRequest(
    var ownerId: Long? = null,

    var status: GroupStatus? = null,
) : QueryRequest()