package com.king250.order.api.module.delivery

import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive

data class PushDeliveryRequest(
    @field:NotBlank
    val type: String,

    @field:NotNull
    @field:Positive
    val addressId: Long,

    @field:NotEmpty
    @field:Valid
    val deliveries: List<@Positive Long>
)
