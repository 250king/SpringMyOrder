package com.king250.order.api.module.address

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class CreateAddressRequest(
    @field:NotNull
    val userId: Long,

    @field:NotBlank
    @field:Size(max = 20)
    val name: String,

    @field:NotBlank
    @field:Pattern(regexp = "^1[3-9]\\d{9}$")
    val phone: String,

    @field:NotBlank
    @field:Size(max = 200)
    val address: String
)
