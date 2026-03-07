package com.king250.order.api.module.address

import com.king250.order.api.common.request.QueryRequest

class AddressQueryRequest(
    var userId: Long? = null,
) : QueryRequest()
