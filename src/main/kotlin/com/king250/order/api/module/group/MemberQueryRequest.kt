package com.king250.order.api.module.group

import com.king250.order.api.common.QueryRequest
import com.king250.order.jooq.enums.Role

class MemberQueryRequest(
    var role: Role? = null,
) : QueryRequest()