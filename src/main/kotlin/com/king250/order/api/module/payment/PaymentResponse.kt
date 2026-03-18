package com.king250.order.api.module.payment

import com.king250.order.api.module.user.UserResponse
import com.king250.order.jooq.enums.PaymentMethod
import com.king250.order.jooq.enums.PaymentType
import java.math.BigDecimal
import java.time.Instant

data class PaymentResponse(
    val id: Long,

    val user: UserResponse,

    val type: PaymentType,

    val referenceId: Long,

    val amount: BigDecimal,

    val currency: String,

    val currencyRate: BigDecimal,

    val method: PaymentMethod?,

    val createdAt: Instant,

    val updatedAt: Instant,

    val paidAt: Instant?
)