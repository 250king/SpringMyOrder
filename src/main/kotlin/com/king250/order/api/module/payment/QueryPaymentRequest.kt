package com.king250.order.api.module.payment

import com.king250.order.api.common.QueryRequest
import com.king250.order.jooq.enums.PaymentMethod
import com.king250.order.jooq.enums.PaymentType

class QueryPaymentRequest(
    var userId: Long? = null,

    var type: PaymentType? = null,

    var referenceId: Long? = null,

    var currency: String? = null,

    var method: PaymentMethod? = null,

    var isPaid: Boolean? = null,
) : QueryRequest()
