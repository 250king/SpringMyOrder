package com.king250.order.api.module.address

import com.king250.order.api.common.QueryRequest

class AddressQueryRequest(
    var userId: Long? = null,
) : QueryRequest()
