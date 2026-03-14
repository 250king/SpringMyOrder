package com.king250.order.api.module.address

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.Size

data class CreateAddressRequest(
    @field:Positive
    var userId: Long?,

    @field:NotBlank
    @field:Size(max = 50)
    @field:Pattern(regexp = "^[a-zA-Z\\u4e00-\\u9fa5\\s]+$")
    val name: String,

    @field:NotBlank
    @field:Size(max = 20)
    @field:Pattern(regexp = "^1[3-9]\\d{9}$")
    val phone: String,

    @field:NotBlank
    @field:Size(max = 200)
    @field:Pattern(regexp = "^[a-zA-Z0-9\\u4e00-\\u9fa5\\s\\-(),.#（）－]+$")
    val address: String
)
