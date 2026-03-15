package com.king250.order.api.module.delivery

import com.king250.order.api.common.QueryRequest
import com.king250.order.jooq.enums.DeliveryCompany
import com.king250.order.jooq.enums.DeliveryStatus

class QueryDeliveryRequest(
    var userId: Long? = null,

    var creatorId: Long? = null,

    var company: DeliveryCompany? = null,

    var status: DeliveryStatus? = null,
): QueryRequest()