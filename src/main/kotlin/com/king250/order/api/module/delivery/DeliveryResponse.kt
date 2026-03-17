package com.king250.order.api.module.delivery

import com.king250.order.api.module.user.UserResponse
import com.king250.order.jooq.enums.DeliveryCompany
import com.king250.order.jooq.enums.DeliveryStatus
import java.time.Instant

data class DeliveryResponse(
    val id: Long,

    val user: UserResponse,

    val name: String?,

    val phone: String?,

    val address: String?,

    val company: DeliveryCompany?,

    val trackingNumber: String?,

    val status: DeliveryStatus,

    val createdAt: Instant,

    val updatedAt: Instant,

    val comment: String?
)