package com.king250.order.api.module.delivery

import com.king250.order.jooq.enums.DeliveryCompany
import jakarta.validation.Valid
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.Size

data class CreateDeliveryRequest(
    @field:Positive
    val userId: Long?,

    @field:Positive
    val addressId: Long?,

    @field:Size(max = 50)
    @field:Pattern(regexp = "^[a-zA-Z\\u4e00-\\u9fa5\\s]+$")
    val name: String?,

    @field:Size(max = 20)
    @field:Pattern(regexp = "^1[3-9]\\d{9}$")
    val phone: String?,

    @field:Size(max = 200)
    @field:Pattern(regexp = "^[a-zA-Z0-9\\u4e00-\\u9fa5\\s\\-(),.#（）－]+$")
    val address: String?,

    val company: DeliveryCompany?,

    @field:Size(max = 200)
    val comment: String?,

    @field:Valid
    @field:NotEmpty
    val lists: List<@NotNull @Positive Long>,
)
