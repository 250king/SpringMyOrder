package com.king250.order.api.module.address

import com.king250.order.api.common.QueryRequest

class QueryAddressRequest(
    var userId: Long? = null,
) : QueryRequest()
